#ifndef HASH_MASK_H
#define HASH_MASK_H

#include <array>
#include <bit>
#include <concepts>
#include <limits>
#include <memory>
#include <variant>

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

	template<MaskType T>
	class HashMask {
		constexpr static std::size_t N{std::max(static_cast<std::size_t>(sizeof(std::unique_ptr<T[]>) / sizeof(T)), static_cast<std::size_t>(1U))};

		using StackMask = std::array<T, N>;
		using HeapMask = std::unique_ptr<T[]>;

		static std::variant<StackMask, HeapMask> makeMask(const std::size_t n) {
			const std::size_t size{maskSize<T>(n)};
			return size <= N ? std::variant<StackMask, HeapMask>{StackMask{}} : std::variant<StackMask, HeapMask>{std::make_unique<T[]>(size)};
		}

		public:
			HashMask() : HashMask{0U} {
			}

			HashMask(const std::size_t n) : mask_{makeMask(n)} {
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
				this->mask_ = makeMask(n);
			}

		private:
			std::variant<StackMask, HeapMask> mask_;
	};
}

#endif //HASH_MASK_H
