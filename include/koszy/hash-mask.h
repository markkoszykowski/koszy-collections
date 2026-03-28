#ifndef HASH_MASK_H
#define HASH_MASK_H

#include <array>
#include <bit>
#include <concepts>
#include <functional>
#include <limits>
#include <memory>
#include <variant>
#include <bits/fs_fwd.h>

namespace koszy::collections::hash {
	// Returns the number of bits in T
	template<typename T> requires std::integral<T>
	constexpr std::size_t bits() {
		return static_cast<std::size_t>(std::numeric_limits<T>::digits);
	}

	template<typename T>
	concept MaskType = std::has_single_bit(bits<T>());

	template<MaskType T>
	constexpr std::size_t shifts() {
		return static_cast<std::size_t>(std::countr_zero(bits<T>()));
	}

	template<MaskType T>
	constexpr std::size_t maskPos(const std::size_t n) {
		return n >> shifts<T>();
	}

	template<MaskType T>
	constexpr std::size_t maskBit(const std::size_t n) {
		return n & static_cast<std::size_t>(bits<T>() - 1U);
	}

	template<MaskType T>
	constexpr std::size_t maskSize(const std::size_t n) {
		return maskPos<T>(n) + static_cast<std::size_t>(static_cast<bool>(maskBit<T>(n)));
	}

	template<typename T, template<typename U> typename A=std::allocator>
	struct Deleter {
		std::reference_wrapper<A<T>> allocator_;
		std::size_t size_;

		Deleter(A<T>& allocator, const std::size_t n) : allocator_{allocator}, size_{n} {
		}

		void operator()(T* pointer) const {
			for (std::size_t i{0U}; i != this->size_; ++i) {
				std::allocator_traits<A<T>>::destroy(this->allocator_, &pointer[i]);
			}
			std::allocator_traits<A<T>>::deallocate(this->allocator_, pointer, this->size_);
		}
	};

	template<
		MaskType T,
		template<typename U> typename A=std::allocator
	>
	class HashMask {
		using HeapMask = std::unique_ptr<T[], Deleter<T, A>>;

		constexpr static std::size_t N{std::max(static_cast<std::size_t>(sizeof(HeapMask) / sizeof(T)), static_cast<std::size_t>(1U))};
		using StackMask = std::array<T, N>;

		static std::variant<StackMask, HeapMask> makeMask(A<T>& allocator, const std::size_t n) {
			const std::size_t size{maskSize<T>(n)};
			if (size <= N) {
				return std::variant<StackMask, HeapMask>{StackMask{}};
			} else {
				T * mask{std::allocator_traits<A<T>>::allocate(allocator, size)};
				for (std::size_t i{0U}; i != size; ++i) {
					std::allocator_traits<A<T>>::construct(allocator, &mask[i]);
				}
				return std::variant<StackMask, HeapMask>{HeapMask{mask, Deleter<T, A>{allocator, size}}};
			}
		}

		public:
			HashMask() : HashMask(0U, A<T>{}) {
			}

			HashMask(const std::size_t n) : HashMask(n, A<T>{}) {
			}

			HashMask(A<T>&& allocator) : HashMask(0U, std::forward<A<T>>(allocator)) {
			}

			HashMask(const std::size_t n, A<T>&& allocator) : allocator_{std::forward<A<T>>(allocator)}, mask_{makeMask(this->allocator_, n)} {
			}

			constexpr bool isSet(const std::size_t n) const {
				return std::visit([n](auto&& mask) -> bool { return static_cast<bool>((mask[maskPos<T>(n)] >> maskBit<T>(n)) & 1U); }, this->mask_);
			}

			constexpr void set(const std::size_t n) {
				std::visit([n](auto&& mask) { mask[maskPos<T>(n)] |= static_cast<T>(1U << maskBit<T>(n)); }, this->mask_);
			}

			constexpr void unset(const std::size_t n) {
				std::visit([n](auto&& mask) { mask[maskPos<T>(n)] &= ~static_cast<T>(1U << maskBit<T>(n)); }, this->mask_);
			}

			constexpr void reset(const std::size_t n) {
				this->mask_ = makeMask(this->allocator_, n);
			}

		private:
			[[no_unique_address]] A<T> allocator_;
			std::variant<StackMask, HeapMask> mask_;
	};
}

#endif //HASH_MASK_H
