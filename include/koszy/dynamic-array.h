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

		Deleter(const Mask& mask, A& allocator, const std::size_t n) : mask{mask}, allocator{allocator}, size{n} {}

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
		using Mask = mask::ArrayMask<M, MA>;
		using Array = std::unique_ptr<T[], Deleter<T, A, M, MA>>;


		constexpr static Array makeArray(A& allocator, const Mask& mask, const std::size_t n) {
			return Array{std::allocator_traits<A>::allocate(allocator, n), Deleter<T, A, M, MA>{allocator, mask, n}};
		}

		public:
			constexpr DynamicArray() : mask_{}, allocator_{}, array_{nullptr, Deleter<T, A, M, MA>{this->mask_, this->allocator_, ZERO}}, size_{ZERO} {}

			constexpr DynamicArray(const std::size_t n) : mask_{n}, allocator_{}, array_{makeArray(this->mask_, this->allocator_, n)}, size_{ZERO} {}

			constexpr DynamicArray(const A& allocator) : mask_{}, allocator_{allocator}, array_{nullptr, Deleter<T, A, M, MA>{this->mask_, this->allocator_, ZERO}}, size_{ZERO} {}

			constexpr DynamicArray(const DynamicArray<T, A, M, MA>& other) = delete;

			constexpr DynamicArray(DynamicArray<T, A, M, MA>&& other) = delete;


			constexpr DynamicArray<T, A, M, MA> operator=(const DynamicArray<T, A, M, MA>& other) = delete;

			constexpr DynamicArray<T, A, M, MA> operator=(DynamicArray<T, A, M, MA>&& other) = delete;


			~DynamicArray() = default;


			[[nodiscard]] constexpr std::size_t capacity() const {
				return this->array_.get_deleter().size;
			}

			[[nodiscard]] constexpr std::size_t size() const {
				return this->size_;
			}

			[[nodiscard]] constexpr bool isSet(const std::size_t i) const {
				return this->mask_.isSet(i);
			}

			[[nodiscard]] constexpr const T& operator[](const std::size_t i) const {
				return this->array_[i];
			}

			[[nodiscard]] constexpr T& operator[](const std::size_t i) {
				return this->array_[i];
			}

		private:
			Mask mask_;
			[[no_unique_address]] A allocator_;
			Array array_;
			std::size_t size_;
	};
}

#endif //DYNAMIC_ARRAY_H
