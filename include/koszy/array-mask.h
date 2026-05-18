#ifndef ARRAY_MASK_H
#define ARRAY_MASK_H

#include <algorithm>
#include <array>
#include <bit>
#include <cstdint>
#include <concepts>
#include <functional>
#include <limits>
#include <memory>
#include <type_traits>
#include <utility>
#include <variant>

#include "koszy/common.h"

namespace koszy::collections::mask {
	template<std::integral T>
	consteval std::size_t bits() {
		return static_cast<std::size_t>(std::numeric_limits<T>::digits);
	}

	template<typename T>
	concept MaskType = std::is_trivial_v<T> && std::has_single_bit(bits<T>());

	template<MaskType T, std::unsigned_integral S=std::size_t>
	consteval S shifts() noexcept {
		return static_cast<S>(std::countr_zero(bits<T>()));
	}

	template<MaskType T, std::unsigned_integral S=std::size_t>
	constexpr S maskPos(const S n) noexcept {
		return n >> shifts<T, S>();
	}

	template<MaskType T, std::unsigned_integral S=std::size_t>
	constexpr S maskBit(const S n) noexcept {
		return n & (static_cast<S>(bits<T>()) - 1U);
	}

	template<MaskType T, std::unsigned_integral S=std::size_t>
	constexpr S maskSize(const S n) noexcept {
		return maskPos<T, S>(n) + static_cast<S>(static_cast<bool>(maskBit<T, S>(n)));
	}

	template<MaskType T, typename A=std::allocator<T>>
	struct ArrayMask {
		using allocator_type = A;
		using value_type = T;
		using pointer_type = std::allocator_traits<allocator_type>::pointer;
		using size_type = std::allocator_traits<allocator_type>::size_type;

		constexpr static value_type ZERO_VALUE{0U};
		constexpr static value_type ONE_VALUE{1U};

		struct DynamicMask : std::pair<pointer_type, size_type> {
			constexpr DynamicMask(const pointer_type first, const size_type second) : std::pair<pointer_type, size_type>(first, second) {}

			constexpr value_type& operator[](const size_type i) {
				return *(this->first + i);
			}

			constexpr const value_type& operator[](const size_type i) const {
				return *(this->first + i);
			}
		};

		constexpr static std::size_t N{std::max(sizeof(DynamicMask) / sizeof(value_type), ONE)};

		struct StaticMask : std::array<value_type, N> {
		};


		static_assert(std::is_same_v<typename StaticMask::size_type, typename std::allocator_traits<allocator_type>::size_type>);


		constexpr static void destroy(allocator_type& allocator, const pointer_type pointer, const size_type n, const size_type size) noexcept(std::is_nothrow_destructible_v<value_type>) {
			if (pointer != nullptr) {
				if constexpr (!std::is_trivially_destructible_v<value_type>) {
					for (size_type i{0U}; i != size; ++i) {
						std::allocator_traits<allocator_type>::destroy(allocator, pointer + i);
					}
				}
				std::allocator_traits<allocator_type>::deallocate(allocator, pointer, n);
			}
		}

		constexpr static void destroy(allocator_type& allocator, const pointer_type pointer, const size_type n) noexcept(std::is_nothrow_destructible_v<value_type>) {
			destroy(allocator, pointer, n, n);
		}

		constexpr static void destroy(allocator_type&, StaticMask&) noexcept(std::is_nothrow_destructible_v<value_type>) {
		}

		constexpr static void destroy(allocator_type& allocator, DynamicMask& mask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			destroy(allocator, std::exchange(mask.first, nullptr), std::exchange(mask.second, 0U));
		}

		constexpr static void destroy(allocator_type& allocator, ArrayMask& arrayMask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			std::visit([&](auto& mask) { destroy(allocator, mask); }, arrayMask.mask);
		}


		struct Guard {
			std::reference_wrapper<allocator_type> allocator;
			pointer_type pointer;
			size_type n;
			size_type size;

			constexpr Guard(allocator_type& allocator, const size_type n) : allocator{allocator}, pointer{std::allocator_traits<allocator_type>::allocate(this->allocator.get(), n)}, n{n}, size{0U} {}

			constexpr Guard(const Guard&) = delete;

			constexpr Guard(Guard&&) = delete;

			constexpr Guard& operator=(const Guard&) = delete;

			constexpr Guard operator=(Guard&&) = delete;

			constexpr ~Guard() noexcept(std::is_nothrow_destructible_v<value_type>) {
				destroy(this->allocator.get(), std::exchange(this->pointer, nullptr), std::exchange(this->n, 0U), std::exchange(this->size, 0U));
			}

			constexpr DynamicMask release() noexcept(std::is_nothrow_move_assignable_v<pointer_type> && std::is_nothrow_move_assignable_v<size_type>) {
				return DynamicMask{std::exchange(this->pointer, nullptr), std::exchange(this->size, 0U)};
			}
		};


		constexpr static DynamicMask makeDynamic(allocator_type& allocator, const size_type size) {
			Guard guard{allocator, size};
			for (; guard.size != guard.n; ++guard.size) {
				std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.size, ZERO_VALUE);
			}
			return guard.release();
		}

		constexpr static DynamicMask copyDynamic(allocator_type& allocator, const DynamicMask& other) {
			Guard guard{allocator, other.second};
			for (; guard.size != guard.n; ++guard.size) {
				std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.size, other[guard.size]);
			}
			return guard.release();
		}

		constexpr static DynamicMask moveDynamic(allocator_type& allocator, DynamicMask&& other) {
			Guard guard{allocator, other.second};
			for (; guard.size != guard.n; ++guard.size) {
				std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.size, std::move(other[guard.size]));
			}
			return guard.release();
		}


		constexpr static std::variant<StaticMask, DynamicMask> makeMask(allocator_type& allocator, const size_type n) {
			const size_type size{maskSize<value_type, size_type>(n)};
			if (size <= N) {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>};
			} else {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, makeDynamic(allocator, size)};
			}
		}


		std::variant<StaticMask, DynamicMask> mask;


		constexpr ArrayMask(allocator_type& allocator) : mask{makeMask(allocator, 0U)} {}

		constexpr ArrayMask(allocator_type& allocator, const size_type n) : mask{makeMask(allocator, n)} {}

		constexpr ArrayMask(const ArrayMask&) = delete;

		constexpr ArrayMask(ArrayMask&&) = default;


		constexpr ArrayMask& operator=(const ArrayMask&) = delete;

		constexpr ArrayMask& operator=(ArrayMask&&) = default;


		constexpr ~ArrayMask() = default;


		constexpr static void copy(allocator_type&, ArrayMask& dstArrayMask, auto&, const StaticMask& srcMask, auto) {
			dstArrayMask.mask.template emplace<StaticMask>(srcMask);
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& dstArrayMask, DynamicMask& dstMask, const StaticMask& srcMask, const std::false_type) {
			destroy(allocator, dstMask);
			dstArrayMask.mask.template emplace<StaticMask>(srcMask);
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, auto&, const DynamicMask& srcMask, auto) {
			arrayMask.mask.template emplace<DynamicMask>(copyDynamic(allocator, srcMask));
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, const DynamicMask& srcMask, const std::false_type) {
			if (const size_type size{dstMask.second}; dstMask.second == srcMask.second) {
				for (size_type i{0U}; i != size; ++i) {
					dstMask[i] = srcMask[i];
				}
			} else {
				destroy(allocator, dstMask);
				arrayMask.mask.template emplace<DynamicMask>(copyDynamic(allocator, srcMask));
			}
		}

		constexpr static void copy(allocator_type& dstAllocator, ArrayMask& dstArrayMask, const allocator_type& srcAllocator, const ArrayMask& srcArrayMask) {
			std::visit(
				[&](auto& dstMask, const auto& srcMask) {
					if constexpr (std::allocator_traits<allocator_type>::propagate_on_container_copy_assignment::value) {
						destroy(dstAllocator, dstMask);
						dstAllocator = srcAllocator;
						copy(dstAllocator, dstArrayMask, dstMask, srcMask, std::true_type{});
					} else {
						copy(dstAllocator, dstArrayMask, dstMask, srcMask, std::false_type{});
					}
				},
				dstArrayMask.mask,
				srcArrayMask.mask
			);
		}


		constexpr static void move(allocator_type&, ArrayMask& dstArrayMask, auto&, StaticMask&& srcMask, auto) {
			dstArrayMask.mask.template emplace<StaticMask>(std::move(srcMask));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& dstArrayMask, DynamicMask& dstMask, StaticMask&& srcMask, const std::false_type) {
			destroy(allocator, dstMask);
			dstArrayMask.mask.template emplace<StaticMask>(std::move(srcMask));
		}

		constexpr static void move(allocator_type&, ArrayMask& arrayMask, auto&, DynamicMask&& srcMask, auto) {
			arrayMask.mask.template emplace<DynamicMask>(std::exchange(srcMask.first, nullptr), std::exchange(srcMask.second, 0U));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, StaticMask&, DynamicMask&& srcMask, const std::false_type) {
			arrayMask.mask.template emplace<DynamicMask>(moveDynamic(allocator, std::move(srcMask)));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, DynamicMask&& srcMask, const std::false_type) {
			if (const size_type size{dstMask.second}; dstMask.second == srcMask.second) {
				for (size_type i{0U}; i != size; ++i) {
					dstMask[i] = std::move(srcMask[i]);
				}
			} else {
				destroy(allocator, dstMask);
				arrayMask.mask.template emplace<DynamicMask>(moveDynamic(allocator, std::move(srcMask)));
			}
		}

		constexpr static void move(allocator_type& dstAllocator, ArrayMask& dstArrayMask, allocator_type&& srcAllocator, ArrayMask&& srcArrayMask) {
			std::visit(
				[&](auto& dstMask, auto&& srcMask) {
					if constexpr (std::allocator_traits<allocator_type>::propagate_on_container_move_assignment::value) {
						destroy(dstAllocator, dstMask);
						dstAllocator = std::move(srcAllocator);
						move(dstAllocator, dstArrayMask, dstMask, std::move(srcMask), std::true_type{});
					} else if constexpr (std::allocator_traits<allocator_type>::is_always_equal::value) {
						destroy(dstAllocator, dstMask);
						move(dstAllocator, dstArrayMask, dstMask, std::move(srcMask), std::true_type{});
					} else {
						if (dstAllocator == srcAllocator) {
							destroy(dstAllocator, dstMask);
							move(dstAllocator, dstArrayMask, dstMask, std::move(srcMask), std::true_type{});
						} else {
							move(dstAllocator, dstArrayMask, dstMask, std::move(srcMask), std::false_type{});
						}
					}
				},
				dstArrayMask.mask,
				std::move(srcArrayMask.mask)
			);
		}


		constexpr static void swap(allocator_type& leftAllocator, ArrayMask& leftArrayMask, allocator_type& rightAllocator, ArrayMask& rightArrayMask) {
			using std::swap;
			if constexpr (std::allocator_traits<allocator_type>::propagate_on_container_swap::value) {
				swap(leftAllocator, rightAllocator);
				swap(leftArrayMask, rightArrayMask);
			} else if constexpr (std::allocator_traits<allocator_type>::is_always_equal::value) {
				swap(leftArrayMask, rightArrayMask);
			} else {
				if (leftAllocator == rightAllocator) {
					swap(leftArrayMask, rightArrayMask);
				} else {
					std::visit(
						Visitor{
							[&](StaticMask& leftMask, StaticMask& rightMask) {
								swap(leftMask, rightMask);
							},
							[&](StaticMask& leftMask, DynamicMask& rightMask) {
								DynamicMask temp{moveDynamic(leftAllocator, rightMask)};
								destroy(rightAllocator, rightMask);
								rightArrayMask.mask.template emplace<StaticMask>(std::move(leftMask));
								leftArrayMask.mask.template emplace<DynamicMask>(std::move(temp));
							},
							[&](DynamicMask& leftMask, StaticMask& rightMask) {
								DynamicMask temp{moveDynamic(rightAllocator, leftMask)};
								destroy(leftAllocator, leftMask);
								leftArrayMask.mask.template emplace<StaticMask>(std::move(rightMask));
								rightArrayMask.mask.template emplace<DynamicMask>(std::move(temp));
							},
							[&](DynamicMask& leftMask, DynamicMask& rightMask) {
								if (const size_type size{leftMask.second}; leftMask.second == rightMask.second) {
									for (size_type i{0U}; i != size; ++i) {
										swap(leftMask[i], rightMask[i]);
									}
								} else {
									DynamicMask leftTemp{moveDynamic(rightAllocator, leftMask)};
									DynamicMask rightTemp{moveDynamic(leftAllocator, rightMask)};
									destroy(leftAllocator, leftMask);
									destroy(rightAllocator, rightMask);
									leftArrayMask.mask.template emplace<StaticMask>(std::move(rightTemp));
									rightArrayMask.mask.template emplace<DynamicMask>(std::move(leftTemp));
								}
							}
						},
						leftArrayMask.mask,
						rightArrayMask.mask
					);
				}
			}
		}


		[[nodiscard]] constexpr bool isSet(const size_type i) const {
			return std::visit([i](const auto& mask) -> bool { return static_cast<bool>((mask[maskPos<value_type, size_type>(i)] >> maskBit<value_type, size_type>(i)) & ONE_VALUE); }, this->mask);
		}

		constexpr void set(const size_type i) {
			std::visit([i](auto& mask) { mask[maskPos<value_type, size_type>(i)] |= (ONE_VALUE << maskBit<value_type, size_type>(i)); }, this->mask);
		}

		constexpr void unset(const size_type i) {
			std::visit([i](auto& mask) { mask[maskPos<value_type, size_type>(i)] &= ~(ONE_VALUE << maskBit<value_type, size_type>(i)); }, this->mask);
		}
	};
}

#endif // ARRAY_MASK_H
