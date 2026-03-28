#ifndef HASH_COMMON_H
#define HASH_COMMON_H

#include <algorithm>
#include <cmath>
#include <cstdint>
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
}

#endif // HASH_COMMON_H
