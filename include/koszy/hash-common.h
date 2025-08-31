#ifndef HASH_COMMON_H
#define HASH_COMMON_H

#include <algorithm>
#include <cmath>
#include <limits>

namespace koszy::collections::hash {
	constexpr std::size_t DEFAULT_INITIAL_CAPACITY{0U};
	constexpr std::size_t DEFAULT_MIN_CAPACITY{16U};
	constexpr float DEFAULT_LOAD_FACTOR{0.75f};

	constexpr bool powerOfTwo(const std::size_t n) {
		return !static_cast<bool>(n & (n - 1U));
	}

	constexpr std::size_t nextPowerOfTwo(const std::size_t n) {
		if ((static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)) < n) [[unlikely]] {
			throw std::invalid_argument{"n is larger than greatest power of two"};
		}

		std::size_t next{n - 1U};
		for (int i{1}; i < std::numeric_limits<std::size_t>::digits; i <<= 1) {
			next |= next >> i;
		}
		return next + 1U;
	}

	constexpr std::size_t maxSize(const std::size_t n, const float f) {
		return std::min(static_cast<std::size_t>(std::ceil(static_cast<long double>(n) * static_cast<long double>(f))), n - 1U);
	}

	constexpr std::size_t arraySize(const std::size_t n, const float f) {
		return nextPowerOfTwo(static_cast<std::size_t>(std::ceil(static_cast<long double>(n) / static_cast<long double>(f))));
	}

	constexpr std::size_t arrayMask(const std::size_t n) {
		return static_cast<std::size_t>(static_cast<bool>(n)) * (n - 1U);
	}
}

#endif // HASH_COMMON_H
