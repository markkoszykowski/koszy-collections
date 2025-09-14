#ifndef HASH_COMMON_H
#define HASH_COMMON_H

#include <algorithm>
#include <bit>
#include <cmath>
#include <cstdint>
#include <limits>
#include <stdexcept>

namespace koszy::collections::hash {
	constexpr std::size_t DEFAULT_INITIAL_CAPACITY{16U};
	constexpr float DEFAULT_LOAD_FACTOR{0.75f};

	constexpr std::size_t nextPowerOfTwo(const std::size_t n) {
		if (constexpr std::size_t max{static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)}; max < n) [[unlikely]] {
			throw std::invalid_argument{"n is larger than greatest power of two"};
		}

		std::size_t next{n};
		--next;
		for (int i{1}; i < std::numeric_limits<std::size_t>::digits; i <<= 1) {
			next |= next >> i;
		}
		++next;

		return next;
	}

	constexpr std::size_t maxSize(const std::size_t n, const float f) {
		return std::min(static_cast<std::size_t>(std::ceil(static_cast<long double>(n) * static_cast<long double>(f))), n - 1U);
	}

	constexpr std::size_t arraySize(const std::size_t n, const float f) {
		return nextPowerOfTwo(static_cast<std::size_t>(std::ceil(static_cast<long double>(n) / static_cast<long double>(f))));
	}

	template<typename T>
	constexpr std::size_t maskSize(const std::size_t n) {
		constexpr std::size_t bits{static_cast<std::size_t>(std::numeric_limits<T>::digits)};

		static_assert(std::has_single_bit(bits));

		constexpr std::size_t shifts{std::numeric_limits<std::size_t>::digits - static_cast<std::size_t>(std::countl_zero(bits)) - 1U};
		constexpr std::size_t mask{bits - 1U};

		return (n >> shifts) + static_cast<std::size_t>(static_cast<bool>(n & mask));
	}

	template<typename T>
	constexpr std::size_t maskPos(const std::size_t n) {
		constexpr std::size_t bits{static_cast<std::size_t>(std::numeric_limits<T>::digits)};

		static_assert(std::has_single_bit(bits));

		constexpr std::size_t shifts{std::numeric_limits<std::size_t>::digits - static_cast<std::size_t>(std::countl_zero(bits)) - 1U};
		constexpr std::size_t mask{bits - 1U};

		return (n >> shifts) + static_cast<std::size_t>(static_cast<bool>(n & mask));
	}

	template<typename T>
	constexpr std::size_t maskBit(const std::size_t n) {
		constexpr std::size_t bits{static_cast<std::size_t>(std::numeric_limits<T>::digits)};

		static_assert(std::has_single_bit(bits));

		constexpr std::size_t mask{bits - 1U};

		return n & mask;
	}
}

#endif // HASH_COMMON_H
