#ifndef HASH_COMMON_H
#define HASH_COMMON_H

#include <algorithm>
#include <cmath>
#include <limits>
#include <stdexcept>
#include <type_traits>

namespace koszy::collections {
	constexpr static std::size_t ZERO{0U};
	constexpr static std::size_t ONE{1U};
	constexpr static std::size_t MAX_POWER_OF_TWO{ONE << (std::numeric_limits<std::size_t>::digits - 1)};

	template <typename... Ts>
	struct Visitor : Ts... {
		using Ts::operator()...;
	};

	template <typename Self, typename Member>
	using like_t = std::conditional_t<
		std::is_const_v<std::remove_reference_t<Self>>,
		std::conditional_t<std::is_lvalue_reference_v<Self&&>, const Member&, const Member&&>,
		std::conditional_t<std::is_lvalue_reference_v<Self&&>, Member&, Member&&>
	>;

	constexpr std::size_t nextPowerOfTwo(const std::size_t n) {
		if (MAX_POWER_OF_TWO < n) [[unlikely]] {
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
		return std::min(static_cast<std::size_t>(std::ceil(static_cast<long double>(n) * static_cast<long double>(f))), n - ONE);
	}

	constexpr std::size_t arraySize(const std::size_t n, const float f) {
		return nextPowerOfTwo(static_cast<std::size_t>(std::ceil(static_cast<long double>(n) / static_cast<long double>(f))));
	}
}

#endif // HASH_COMMON_H
