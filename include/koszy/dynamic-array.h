#ifndef DYNAMIC_ARRAY_H
#define DYNAMIC_ARRAY_H

#include <algorithm>
#include <cstdint>
#include <functional>
#include <memory>
#include <type_traits>
#include <utility>

#include "koszy/common.h"
#include "koszy/array-mask.h"

namespace koszy::collections::array {
	template <typename T, typename A=std::allocator<T>>
	struct DynamicArray {
		using value_type = T;
		using allocator_type = A;
		using size_type = std::allocator_traits<allocator_type>::size_type;
		using difference_type = std::allocator_traits<allocator_type>::difference_type;
		using reference = value_type&;
		using const_reference = const value_type&;
		using pointer = std::allocator_traits<allocator_type>::pointer;
		using const_pointer = std::allocator_traits<allocator_type>::const_pointer;

		pointer begin;
		pointer end;

		[[nodiscard]] constexpr pointer data() & noexcept {
			return this->begin;
		}

		[[nodiscard]] constexpr const_pointer data() const & noexcept {
			return this->begin;
		}

		[[nodiscard]] constexpr size_type size() const & noexcept {
			const difference_type size{this->end - this->begin};
			if (size < 0) {
				std::unreachable();
			}
			return static_cast<size_type>(size);
		}

		template <typename Self>
		[[nodiscard]] constexpr like_t<Self, value_type> operator[](this Self&& self, const size_type i) noexcept {
			return std::forward_like<Self>(*(self.data + i));
		}

		template <typename Value> requires std::is_same_v<std::remove_cvref_t<Value>, value_type>
		constexpr void insert(allocator_type& allocator, const size_type i, Value&& value) {
			std::allocator_traits<A>::construct(allocator, std::to_address(this->begin + i), std::forward<Value>(value));
		}

		template <typename... Args>
		constexpr void emplace(allocator_type& allocator, const size_type i, Args&&... args) {
			std::allocator_traits<A>::construct(allocator, std::to_address(this->begin + i), std::forward<Args>(args)...);
		}

		constexpr void erase(allocator_type& allocator, const size_type i) {
			std::allocator_traits<A>::destroy(allocator, std::to_address(this->begin + i));
		}


		template <typename M, typename MA>
		constexpr static void destroy(allocator_type& allocator, const pointer data, const size_type size, const mask::ArrayMask<M, MA>& mask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			if (data != nullptr) {
				if constexpr (!std::is_trivially_destructible_v<value_type>) {
					for (size_type i{0U}; i != size; ++i) {
						if (mask.isSet(i)) {
							std::allocator_traits<allocator_type>::destroy(allocator, std::to_address(data + i));
						}
					}
				}
				std::allocator_traits<allocator_type>::deallocate(allocator, data, size);
			}
		}

		template <typename M, typename MA>
		constexpr static void destroy(allocator_type& allocator, DynamicArray& array, const mask::ArrayMask<M, MA>& mask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			destroy(allocator, std::exchange(array.pointer, nullptr), std::exchange(array.n, 0U), mask);
		}


		template <typename M, typename MA>
		struct Guard {
			using mask_type = mask::ArrayMask<M, MA>;

			std::reference_wrapper<allocator_type> allocator;
			pointer_type pointer;
			size_type size;
			std::reference_wrapper<const mask_type> mask;

			constexpr Guard(allocator_type& allocator, const size_type size, const mask_type& mask) : allocator{allocator},
				pointer{std::allocator_traits<allocator_type>::allocate(this->allocator.get(), size)},
				size{size},
				mask{mask}
			{}

			constexpr Guard(const Guard&) = delete;

			constexpr Guard(Guard&& other) noexcept : allocator{std::move(other.allocator)},
				pointer{std::exchange(other.pointer, nullptr)},
				size{std::exchange(other.size, 0U)},
				mask{std::move(other.mask)}
			{}

			constexpr Guard& operator=(const Guard&) = delete;

			constexpr Guard& operator=(Guard&& other) noexcept {
				this->allocator = std::move(other.allocator);
				this->pointer = std::exchange(other.pointer, nullptr);
				this->size = std::exchange(other.size, 0U);
				this->mask = std::move(other.mask);
				return *this;
			}

			constexpr ~Guard() noexcept(std::is_nothrow_destructible_v<value_type>) {
				destroy(this->allocator.get(), this->pointer, this->size, this->mask.get());
			}

			constexpr DynamicArray release() noexcept {
				return DynamicArray{std::exchange(this->pointer, nullptr), std::exchange(this->size, 0U)};
			}
		};


		template <typename Array> requires std::is_same_v<std::remove_cvref_t<Array>, DynamicArray>
		constexpr static Guard cloneDynamic(allocator_type& allocator, Array&& other) {
			Guard guard{allocator, other.size()};
			for (; guard.end != guard.size; ++guard.end) {
				std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.end, std::forward<Array>(other)[guard.end]);
			}
			return guard;
		}


		constexpr DynamicArray(allocator_type& allocator) : array_{nullptr, Deleter{}} {}

		constexpr DynamicArray(const std::size_t n) : array_{nullptr, Deleter{}} {
			Deleter& deleter{this->array_.get_deleter()};
			this->array_.reset(std::allocator_traits<A>::allocate(deleter.allocator, n));
			deleter.size = n;
			deleter.mask.reset(n);
		}

		constexpr DynamicArray(const A& allocator) : array_{nullptr, Deleter{allocator}} {}

		constexpr DynamicArray(const std::size_t n, const A& allocator) : array_{nullptr, Deleter{allocator}} {
			Deleter& deleter{this->array_.get_deleter()};
			this->array_.reset(std::allocator_traits<A>::allocate(deleter.allocator, n));
			deleter.size = n;
			deleter.mask.reset(n);
		}

		constexpr DynamicArray(const DynamicArray<T, A, M>& that) : array_{nullptr, Deleter{std::allocator_traits<A>::select_on_container_copy_construction(that.array_.get_deleter().allocator)}} {
			const std::size_t size{that.array_.get_deleter().size};
			if (size != ZERO) {
				Deleter& thisDeleter{this->array_.get_deleter()};
				this->array_.reset(std::allocator_traits<A>::allocate(thisDeleter.allocator, size));
				thisDeleter.size = size;
				thisDeleter.mask.reset(size);

				const Deleter& thatDeleter{that.array_.get_deleter()};
				for (std::size_t i{ZERO}; i != size; ++i) {
					if (thatDeleter.mask.isSet(i)) {
						std::allocator_traits<A>::construct(thisDeleter.allocator, std::addressof(this->array_.get()[i]), that.array_.get()[i]);
					}
				}
			}
		}

		constexpr DynamicArray(DynamicArray<T, A, M>&& other) noexcept : allocator_{std::move(other.allocator_)}, mask_{std::move(other.mask_)}, array_{moveArray<true>(*this, std::move(other.array_))}, size_{other.size_} {}


		constexpr DynamicArray<T, A, M>& operator=(const DynamicArray<T, A, M>& other) {
			if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
				this->array_.reset();
				this->mask_ = other.mask_;
				this->allocator_ = other.allocator_;
				this->array_ = copyMask(*this, other.array_);
			} else {
				const std::size_t thisSize{this->array_.get_deleter().size};
				const std::size_t otherSize{other.array_.get_deleter().size};
				if (const std::size_t size{thisSize}; thisSize == otherSize) {
					for (std::size_t i{ZERO}; i != size; ++i) {
						if (this->mask_.isSet(i)) {
							if (other.mask_.isSet(i)) {
								this->array_[i] = other.array_[i];
							} else {
								std::allocator_traits<A>::destroy(this->allocator_, std::addressof(this->array_[i]));
							}
						} else {
							if (other.mask_.isSet(i)) {
								std::allocator_traits<A>::construct(this->allocator_, std::addressof(this->array_[i]), other.array_[i]);
							} else {
								// nothing
							}
						}
					}
					this->mask_ = other.mask_;
				} else {
					this->array_.reset();
					this->mask_ = other.mask_;
					this->array_ = copyMask(*this, other.array_);
				}
			}
			return *this;
		}

		constexpr DynamicArray<T, A, M>& operator=(DynamicArray<T, A, M>&& other) noexcept {
			if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
				this->array_.reset();
				this->mask_ = std::move(other.mask_);
				this->allocator_ = std::move(other.allocator_);
				this->array_ = moveArray<true>(*this, std::move(other.array_));
			} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
				this->array_.reset();
				this->mask_ = std::move(other.mask_);
				this->array_ = moveArray<true>(*this, std::move(other.array_));
			} else {
				const std::size_t thisSize{this->array_.get_deleter().size};
				const std::size_t otherSize{other.array_.get_deleter().size};
				if (const std::size_t size{thisSize}; thisSize == otherSize) {
					for (std::size_t i{ZERO}; i != size; ++i) {
						if (this->mask_.isSet(i)) {
							if (other.mask_.isSet(i)) {
								this->array_[i] = std::move(other.array_[i]);
							} else {
								std::allocator_traits<A>::destroy(this->allocator_, std::addressof(this->array_[i]));
							}
						} else {
							if (other.mask_.isSet(i)) {
								std::allocator_traits<A>::construct(this->allocator_, std::addressof(this->array_[i]), std::move(other.array_[i]));
							} else {
								// nothing
							}
						}
					}
					this->mask_ = std::move(other.mask_);
				} else {
					this->array_.reset();
					this->mask_ = std::move(other.mask_);
					this->array_ = moveArray<false>(*this, std::move(other.array_));
				}
			}
			return *this;
		}


		~DynamicArray() = default;


		constexpr friend void swap(DynamicArray<T, A, M>& a, DynamicArray<T, A, M>& b) noexcept {
			using std::swap;
			if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
				const std::size_t aSize{a.array_.get_deleter().size};
				const std::size_t bSize{b.array_.get_deleter().size};
				T* const aTemp{a.array_.release()};
				T* const bTemp{b.array_.release()};
				swap(a.mask_, b.mask_);
				swap(a.allocator_, b.allocator_);
				a.array_ = Array{bTemp, Deleter{a, bSize}};
				b.array_ = Array{aTemp, Deleter{b, aSize}};
			} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
				const std::size_t aSize{a.array_.get_deleter().size};
				const std::size_t bSize{b.array_.get_deleter().size};
				T* const aTemp{a.array_.release()};
				T* const bTemp{b.array_.release()};
				swap(a.mask_, b.mask_);
				a.array_ = Array{bTemp, Deleter{a, bSize}};
				b.array_ = Array{aTemp, Deleter{b, aSize}};
			} else {
				const std::size_t aSize{a.array_.get_deleter().size};
				const std::size_t bSize{b.array_.get_deleter().size};
				if (const std::size_t size{aSize}; aSize == bSize) {
					for (std::size_t i{ZERO}; i != size; ++i) {
						if (a.mask_.isSet(i)) {
							if (b.mask_.isSet(i)) {
								swap(a.array_[i], b.array_[i]);
							} else {
								std::allocator_traits<A>::construct(b.allocator_, std::addressof(b.array_[i]), std::move(a.array_[i]));
								std::allocator_traits<A>::destroy(a.allocator_, std::addressof(a.array_[i]));
							}
						} else {
							if (b.mask_.isSet(i)) {
								std::allocator_traits<A>::construct(a.allocator_, std::addressof(a.array_[i]), std::move(b.array_[i]));
								std::allocator_traits<A>::destroy(b.allocator_, std::addressof(b.array_[i]));
							} else {
								// nothing
							}
						}
					}
					swap(a.mask_, b.mask_);
				} else {
					T* const aTemp{std::allocator_traits<A>::allocate(b.allocator_, aSize)};
					T* const bTemp{std::allocator_traits<A>::allocate(a.allocator_, bSize)};
					for (std::size_t i{ZERO}; i != std::max(aSize, bSize); ++i) {
						if (i < aSize && a.mask_.isSet(i)) {
							std::allocator_traits<A>::construct(b.allocator_, std::addressof(aTemp[i]), std::move(a.array_[i]));
						}
						if (i < bSize && b.mask_.isSet(i)) {
							std::allocator_traits<A>::construct(a.allocator_, std::addressof(bTemp[i]), std::move(b.array_[i]));
						}
					}
					swap(a.mask_, b.mask_);
					a.array_ = Array{bTemp, Deleter{a, bSize}};
					b.array_ = Array{aTemp, Deleter{b, aSize}};
				}
			}
		}
	};
}

#endif // DYNAMIC_ARRAY_H
