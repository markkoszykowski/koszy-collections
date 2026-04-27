#ifndef DYNAMIC_ARRAY_H
#define DYNAMIC_ARRAY_H

#include <cstdint>
#include <functional>
#include <memory>
#include <utility>

#include "koszy/common.h"
#include "koszy/array-mask.h"

namespace koszy::collections::array {
	template<typename T, typename A, typename M, typename MA>
	struct Deleter {
		using Mask = mask::ArrayMask<M, MA>;

		std::reference_wrapper<const Mask> mask;
		std::reference_wrapper<A> allocator;
		std::size_t size;

		constexpr Deleter(const Mask& mask, A& allocator, const std::size_t n) : mask{mask}, allocator{allocator}, size{n} {}

		constexpr void operator()(T* const pointer) {
			for (std::size_t i{ZERO}; i != this->size; ++i) {
				if (this->mask.get().isSet(i)) {
					std::allocator_traits<A>::destroy(this->allocator.get(), std::addressof(pointer[i]));
				}
			}
			std::allocator_traits<A>::deallocate(this->allocator.get(), pointer, this->size);
		}
	};


	template<typename T, typename A=std::allocator<T>, typename M=std::uintptr_t, typename MA=std::allocator<M>>
	class DynamicArray {
		using allocator_type = A;
		using value_type = T;

		using Mask = mask::ArrayMask<M, MA>;
		using Array = std::unique_ptr<T[], Deleter<T, A, M, MA>>;


		constexpr static Array makeArray(const Mask& mask, A& allocator) {
			return Array{nullptr, Deleter<T, A, M, MA>{mask, allocator, ZERO}};
		}

		constexpr static Array makeArray(const Mask& mask, A& allocator, const std::size_t n) {
			return Array{std::allocator_traits<A>::allocate(allocator, n), Deleter<T, A, M, MA>{mask, allocator, n}};
		}

		constexpr static Array copyArray(const Mask& mask, A& allocator, const Array& other) {
			if (other.get() == nullptr) {
				return makeArray(mask, allocator);
			}
			const std::size_t size{other.get_deleter().size};
			T* const array{std::allocator_traits<A>::allocate(allocator, size)};
			for (std::size_t i{ZERO}; i != size; ++i) {
				if (mask.isSet(i)) {
					std::allocator_traits<A>::construct(allocator, std::addressof(array[i]), other[i]);
				}
			}
			return Array{array, Deleter<T, A, M, MA>{mask, allocator, size}};
		}

		template<bool Move>
		constexpr static Array moveArray(const Mask& mask, A& allocator, Array&& other) {
			if (other.get() == nullptr) {
				return makeArray(mask, allocator);
			}
			if constexpr (Move) {
				return Array{other.release(), Deleter<T, A, M, MA>{allocator, mask, other.get_deleter().size}};
			} else {
				const std::size_t size{other.get_deleter().size};
				T* const array{std::allocator_traits<A>::allocate(allocator, size)};
				for (std::size_t i{ZERO}; i != size; ++i) {
					if (mask.isSet(i)) {
						std::allocator_traits<A>::construct(allocator, std::addressof(array[i]), other[i]);
					}
				}
				return Array{array, Deleter<T, A, M, MA>{mask, allocator, size}};
			}
		}

		public:
			constexpr DynamicArray() : mask_{}, allocator_{}, array_{makeArray(this->mask_, this->allocator_)}, size_{ZERO} {}

			constexpr DynamicArray(const std::size_t n) : mask_{n}, allocator_{}, array_{makeArray(this->mask_, this->allocator_, n)}, size_{ZERO} {}

			constexpr DynamicArray(const A& allocator) : mask_{}, allocator_{allocator}, array_{makeArray(this->mask_, this->allocator_)}, size_{ZERO} {}

			constexpr DynamicArray(const std::size_t n, const A& allocator) : mask_{}, allocator_{allocator}, array_{makeArray(this->mask_, this->allocator_, n)}, size_{ZERO} {}

			constexpr DynamicArray(const DynamicArray<T, A, M, MA>& other) : mask_{other.mask_}, allocator_{std::allocator_traits<A>::select_on_container_copy_construction(other.allocator_)}, array_{copyArray(this->mask_, this->allocator_, other.array_)}, size_{other.size_} {}

			constexpr DynamicArray(DynamicArray<T, A, M, MA>&& other) noexcept : mask_{std::move(other.mask_)}, allocator_{std::move(other.allocator_)}, array_{moveArray<true>(this->mask_, this->allocator_, std::move(other.array_))}, size_{other.size_} {}


			constexpr DynamicArray<T, A, M, MA>& operator=(const DynamicArray<T, A, M, MA>& other) {
				if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
					this->array_.reset();
					this->mask_ = other.mask_;
					this->allocator_ = other.allocator_;
					this->array_ = copyMask(this->mask_, this->allocator_, other.array_);
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
						this->array_ = copyMask(this->mask_, this->allocator_, other.array_);
					}
				}
				return *this;
			}

			constexpr DynamicArray<T, A, M, MA>& operator=(DynamicArray<T, A, M, MA>&& other) noexcept {
				if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
					this->array_.reset();
					this->mask_ = std::move(other.mask_);
					this->allocator_ = std::move(other.allocator_);
					this->array_ = moveArray<true>(this->mask_, this->allocator_, std::move(other.array_));
				} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
					this->array_.reset();
					this->mask_ = std::move(other.mask_);
					this->array_ = moveArray<true>(this->mask_, this->allocator_, std::move(other.array_));
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
						this->array_ = moveArray<false>(this->mask_, this->allocator_, std::move(other.array_));
					}
				}
				return *this;
			}


			~DynamicArray() = default;


			constexpr friend void swap(DynamicArray<T, A, M, MA>& a, DynamicArray<T, A, M, MA>& b) noexcept {
				using std::swap;
				if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
					const std::size_t aSize{a.array_.get_deleter().size};
					const std::size_t bSize{b.array_.get_deleter().size};
					T* const aTemp{a.array_.release()};
					T* const bTemp{b.array_.release()};
					swap(a.mask_, b.mask_);
					swap(a.allocator_, b.allocator_);
					a.array_ = Array{bTemp, Deleter<T, A, M, MA>{a.mask_, a.allocator_, bSize}};
					b.array_ = Array{aTemp, Deleter<T, A, M, MA>{b.mask_, b.allocator_, aSize}};
				} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
					const std::size_t aSize{a.array_.get_deleter().size};
					const std::size_t bSize{b.array_.get_deleter().size};
					T* const aTemp{a.array_.release()};
					T* const bTemp{b.array_.release()};
					swap(a.mask_, b.mask_);
					a.array_ = Array{bTemp, Deleter<T, A, M, MA>{a.mask_, a.allocator_, bSize}};
					b.array_ = Array{aTemp, Deleter<T, A, M, MA>{b.mask_, b.allocator_, aSize}};
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
						a.array_ = Array{bTemp, Deleter<T, A, M, MA>{a.mask_, a.allocator_, bSize}};
						b.array_ = Array{aTemp, Deleter<T, A, M, MA>{b.mask_, b.allocator_, aSize}};
					}
				}
			}

			[[nodiscard]] constexpr std::size_t capacity() const {
				return this->array_.get_deleter().size;
			}

			[[nodiscard]] constexpr std::size_t empty() const {
				return this->size_ == ZERO;
			}

			[[nodiscard]] constexpr std::size_t size() const {
				return this->size_;
			}

			[[nodiscard]] constexpr std::size_t maxSize() const {
				return MAX_POWER_OF_TWO;
			}

			[[nodiscard]] constexpr bool contains(const std::size_t i) const {
				return this->mask_.isSet(i);
			}

			[[nodiscard]] constexpr const T& operator[](const std::size_t i) const {
				return this->array_[i];
			}

			[[nodiscard]] constexpr T& operator[](const std::size_t i) {
				return this->array_[i];
			}

			constexpr void insert(const std::size_t i, const T& value) {
				std::allocator_traits<A>::construct(this->allocator_, std::addressof(this->array_[i]), value);
				this->mask_.set(i);
				++this->size_;
			}

			constexpr void insert(const std::size_t i, T&& value) {
				std::allocator_traits<A>::construct(this->allocator_, std::addressof(this->array_[i]), std::move(value));
				this->mask_.set(i);
				++this->size_;
			}

			template<typename... Args>
			constexpr void emplace(const std::size_t i, Args&&... args) {
				std::allocator_traits<A>::construct(this->allocator_, std::addressof(this->array_[i]), std::forward<Args>(args)...);
				this->mask_.set(i);
				++this->size_;
			}

			constexpr void erase(const std::size_t i) {
				std::allocator_traits<A>::destroy(this->allocator_, std::addressof(this->array_[i]));
				this->mask_.unset(i);
				--this->size_;
			}

			constexpr T extract(const std::size_t i) {
				const T value{std::move(this->array_[i])};
				std::allocator_traits<A>::destroy(this->allocator_, std::addressof(this->array_[i]));
				this->mask_.unset(i);
				--this->size_;
				return value;
			}

		private:
			Mask mask_;
			[[no_unique_address]] A allocator_;
			Array array_;
			std::size_t size_;
	};
}

#endif // DYNAMIC_ARRAY_H
