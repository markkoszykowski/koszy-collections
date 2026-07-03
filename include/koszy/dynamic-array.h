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
			return std::forward_like<Self>(*(self.begin + i));
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
		constexpr static void destroy(allocator_type& allocator, const mask::ArrayMask<M, MA>& mask, const pointer data, const size_type size) noexcept(std::is_nothrow_destructible_v<value_type>) {
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
		constexpr static void destroy(allocator_type& allocator, const mask::ArrayMask<M, MA>& mask, DynamicArray& array) noexcept(std::is_nothrow_destructible_v<value_type>) {
			destroy(allocator, mask, array.data(), array.size());
		}


		template <typename M, typename MA>
		struct Guard {
			using mask_type = mask::ArrayMask<M, MA>;

			std::reference_wrapper<allocator_type> allocator;
			std::reference_wrapper<const mask_type> mask;
			pointer data;
			size_type size;

			constexpr Guard(allocator_type& allocator, const size_type size, const mask_type& mask) : allocator{allocator},
				mask{mask},
				data{std::allocator_traits<allocator_type>::allocate(allocator, size)},
				size{size}
			{}

			constexpr Guard(const Guard&) = delete;

			constexpr Guard(Guard&& other) noexcept : allocator{std::move(other.allocator)},
				mask{std::move(other.mask)},
				data{std::exchange(other.data, nullptr)},
				size{std::exchange(other.size, 0U)}
			{}

			constexpr Guard& operator=(const Guard&) = delete;

			constexpr Guard& operator=(Guard&& other) noexcept {
				destroy(this->allocator.get(), this->mask.get(), this->data, this->size);

				this->allocator = std::move(other.allocator);
				this->mask = std::move(other.mask);
				this->data = std::exchange(other.data, nullptr);
				this->size = std::exchange(other.size, 0U);

				return *this;
			}

			constexpr ~Guard() noexcept {
				destroy(this->allocator.get(), this->mask.get(), this->data, this->size);
			}

			constexpr DynamicArray release() noexcept {
				const pointer data{std::exchange(this->data, nullptr)};
				const size_type size{std::exchange(this->size, 0U)};
				return DynamicArray{data, size};
			}
		};


		template <typename Array, typename M, typename MA, typename OM, typename OMA> requires std::is_same_v<std::remove_cvref_t<Array>, DynamicArray>
		[[nodiscard]] constexpr static Guard<M, MA> guard(allocator_type& allocator, mask::ArrayMask<M, MA>& mask, const mask::ArrayMask<OM, OMA>& otherMask, Array&& otherArray) {
			Guard guard{allocator, otherArray.size(), mask};
			for (size_type i{0U}; i != guard.size; ++i) {
				if (otherMask.isSet(i)) {
					std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.end, std::forward<Array>(otherArray)[i]);
					mask.set(i);
				}
			}
			return guard;
		}

		template <typename Array, typename M, typename MA, typename OM, typename OMA> requires std::is_same_v<std::remove_cvref_t<Array>, DynamicArray>
		[[nodiscard]] constexpr static Guard<M, MA> guard(allocator_type& allocator, const mask::ArrayMask<M, MA>& mask, const mask::ArrayMask<OM, OMA>& otherMask, Array&& otherArray) {
			Guard guard{allocator, otherArray.size(), mask};
			for (size_type i{0U}; i != guard.size; ++i) {
				if (otherMask.isSet(i)) {
					std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.end, std::forward<Array>(otherArray)[i]);
				}
			}
			return guard;
		}

		template <typename Array, typename M, typename MA, typename OM, typename OMA> requires std::is_same_v<std::remove_cvref_t<Array>, DynamicArray>
		[[nodiscard]] constexpr static DynamicArray clone(allocator_type& allocator, mask::ArrayMask<M, MA>& mask, const mask::ArrayMask<OM, OMA>& otherMask, Array&& otherArray) {
			return guard(allocator, mask, otherMask, std::forward<Array>(otherArray)).release();
		}

		template <typename Array, typename M, typename MA, typename OM, typename OMA> requires std::is_same_v<std::remove_cvref_t<Array>, DynamicArray>
		[[nodiscard]] constexpr static DynamicArray clone(allocator_type& allocator, const mask::ArrayMask<M, MA>& mask, const mask::ArrayMask<OM, OMA>& otherMask, Array&& otherArray) {
			return guard(allocator, mask, otherMask, std::forward<Array>(otherArray)).release();
		}


		[[nodiscard]] constexpr static pointer construct(allocator_type& allocator, const size_type n) {
			return n == 0U ? nullptr : std::allocator_traits<allocator_type>::allocate(allocator, n);
		}


		constexpr explicit DynamicArray(allocator_type&) : begin{nullptr}, end{nullptr} {}

		constexpr DynamicArray(allocator_type& allocator, const size_type n) : begin{construct(allocator, n)}, end{this->begin + n} {}

		constexpr DynamicArray(const pointer begin, const pointer end) : begin{begin}, end{end} {}


		template <typename M, typename MA>
		constexpr static DynamicArray copy(allocator_type& allocator, const mask::ArrayMask<M, MA>& mask, const DynamicArray& other) {
			return clone(allocator, mask, mask, other);
		}


		constexpr static DynamicArray move(DynamicArray&& other) {
			return DynamicArray{std::exchange(other.begin, nullptr), std::exchange(other.end, nullptr)};
		}
	};

	template <typename T, typename A, typename M, typename MA>
	struct Guard {
		std::reference_wrapper<A> allocator;
		std::reference_wrapper<const mask::ArrayMask<M, MA>> mask;
		DynamicArray<T, A> array;

		constexpr explicit Guard(DynamicArray<T, A>::allocator_type& allocator, const mask::ArrayMask<M, MA>& mask) : allocator{allocator}, mask{mask}, array{allocator} {}

		constexpr explicit Guard(DynamicArray<T, A>::allocator_type& allocator, const mask::ArrayMask<M, MA>& mask, const DynamicArray<T, A>::size_type n) : allocator{allocator}, mask{mask}, array{allocator, n} {}

		constexpr Guard(const Guard&) = delete;

		constexpr Guard(Guard&& other) noexcept : allocator{std::move(other.allocator)}, mask{std::move(other.mask)}, array{DynamicArray<T, A>::move(std::move(other.array))} {}

		constexpr Guard& operator=(const Guard&) = delete;

		constexpr Guard& operator=(Guard&& other) noexcept {
			DynamicArray<T, A>::destroy(this->allocator.get(), this->mask.get(), this->array);

			this->allocator = std::move(other.allocator);
			this->mask = std::move(other.mask);
			this->array = DynamicArray<T, A>::move(std::move(other.array));

			return *this;
		}

		constexpr ~Guard() noexcept {
			DynamicArray<T, A>::destroy(this->allocator.get(), this->mask.get(), this->array);
		}

		[[nodiscard]] constexpr DynamicArray<T, A> release() noexcept {
			return DynamicArray<T, A>::move(std::move(this->array));
		}
	};
}

#endif // DYNAMIC_ARRAY_H
