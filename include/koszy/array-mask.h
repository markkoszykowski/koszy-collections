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
		using const_pointer = std::allocator_traits<allocator_type>::const_pointer;
		using size_type = std::allocator_traits<allocator_type>::size_type;

		constexpr static value_type ZERO_VALUE{0U};
		constexpr static value_type ONE_VALUE{1U};

		struct DynamicMask {
			std::pair<pointer_type, size_type> mask;

			constexpr DynamicMask(const pointer_type first, const size_type second) : mask{first, second} {}

			constexpr DynamicMask(const DynamicMask&) = delete;

			constexpr DynamicMask(DynamicMask&& other) noexcept : mask{std::exchange(other.mask.first, nullptr), std::exchange(other.mask.second, 0U)} {}

			constexpr DynamicMask& operator=(const DynamicMask&) = delete;

			constexpr DynamicMask& operator=(DynamicMask&& other) noexcept {
				this->mask = std::make_pair(std::exchange(other.mask.first, nullptr), std::exchange(other.mask.second, 0U));
				return *this;
			}

			[[nodiscard]] constexpr pointer_type data() & noexcept {
				return this->mask.first;
			}

			[[nodiscard]] constexpr const_pointer data() const & noexcept {
				return this->mask.first;
			}

			[[nodiscard]] constexpr size_type size() const & noexcept {
				return this->mask.second;
			}

			[[nodiscard]] constexpr value_type& operator[](const size_type i) & noexcept {
				return *(this->mask.first + i);
			}

			[[nodiscard]] constexpr const value_type& operator[](const size_type i) const & noexcept {
				return *(this->mask.first + i);
			}

			[[nodiscard]] constexpr value_type&& operator[](const size_type i) && noexcept {
				return std::move(*(this->mask.first + i));
			}
		};

		constexpr static std::size_t N{std::max(sizeof(DynamicMask) / sizeof(value_type), ONE)};

		struct StaticMask {
			std::array<value_type, N> mask;

			[[nodiscard]] constexpr pointer_type data() & noexcept {
				return this->mask.data();
			}

			[[nodiscard]] constexpr const_pointer data() const & noexcept {
				return this->mask.data();
			}

			[[nodiscard]] constexpr size_type size() const & noexcept {
				return this->mask.size();
			}

			[[nodiscard]] constexpr value_type& operator[](const size_type i) & noexcept {
				return this->mask[i];
			}

			[[nodiscard]] constexpr const value_type& operator[](const size_type i) const & noexcept {
				return this->mask[i];
			}

			[[nodiscard]] constexpr value_type&& operator[](const size_type i) && noexcept {
				return std::move(this->mask[i]);
			}
		};


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
			destroy(allocator, std::exchange(mask.mask.first, nullptr), std::exchange(mask.mask.second, 0U));
		}

		constexpr static void destroy(allocator_type& allocator, ArrayMask& arrayMask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			std::visit([&](auto& mask) { destroy(allocator, mask); }, arrayMask.mask);
		}


		struct Guard {
			std::reference_wrapper<allocator_type> allocator;
			pointer_type pointer;
			size_type n;
			size_type size;

			constexpr Guard(allocator_type& allocator, const size_type n) : allocator{allocator},
				pointer{std::allocator_traits<allocator_type>::allocate(this->allocator.get(), n)},
				n{n},
				size{0U}
			{}

			constexpr Guard(const Guard&) = delete;

			constexpr Guard(Guard&& other) noexcept : allocator{std::move(other.allocator)},
				pointer{std::exchange(other.pointer, nullptr)},
				n{std::exchange(other.n, 0U)},
				size{std::exchange(other.size, 0U)}
			{}

			constexpr Guard& operator=(const Guard&) = delete;

			constexpr Guard& operator=(Guard&& other) noexcept {
				this->allocator = std::move(other.allocator);
				this->pointer = std::exchange(other.pointer, nullptr);
				this->n = std::exchange(other.n, 0U);
				this->size = std::exchange(other.size, 0U);
				return *this;
			}

			constexpr ~Guard() noexcept(std::is_nothrow_destructible_v<value_type>) {
				destroy(this->allocator.get(), std::exchange(this->pointer, nullptr), std::exchange(this->n, 0U), std::exchange(this->size, 0U));
			}

			constexpr DynamicMask release() noexcept {
				pointer_type pointer{std::exchange(this->pointer, nullptr)};
				size_type n{std::exchange(this->n, 0U)};
				size_type size{std::exchange(this->size, 0U)};
				return DynamicMask{pointer, n};
			}
		};


		constexpr static Guard makeDynamic(allocator_type& allocator, const size_type size) {
			Guard guard{allocator, size};
			for (; guard.size != guard.n; ++guard.size) {
				std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.size, ZERO_VALUE);
			}
			return guard;
		}

		template<typename DynamicMask>
		constexpr static Guard cloneDynamic(allocator_type& allocator, DynamicMask&& other) {
			Guard guard{allocator, other.size()};
			for (; guard.size != guard.n; ++guard.size) {
				std::allocator_traits<allocator_type>::construct(allocator, guard.pointer + guard.size, std::forward<DynamicMask>(other)[guard.size]);
			}
			return guard;
		}


		constexpr static std::variant<StaticMask, DynamicMask> makeMask(allocator_type& allocator, const size_type n) {
			const size_type size{maskSize<value_type, size_type>(n)};
			if (size <= N) {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>};
			} else {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, makeDynamic(allocator, size).release()};
			}
		}


		std::variant<StaticMask, DynamicMask> mask;


		constexpr ArrayMask(allocator_type& allocator) : mask{makeMask(allocator, 0U)} {}

		constexpr ArrayMask(allocator_type& allocator, const size_type n) : mask{makeMask(allocator, n)} {}

		constexpr ArrayMask(const ArrayMask&) = delete;

		constexpr ArrayMask(ArrayMask&&) noexcept = default;


		constexpr ArrayMask& operator=(const ArrayMask&) = delete;

		constexpr ArrayMask& operator=(ArrayMask&&) noexcept = default;


		constexpr ~ArrayMask() noexcept = default;


		constexpr static void copy(allocator_type&, ArrayMask& dstArrayMask, auto&, const StaticMask& srcMask, auto) {
			dstArrayMask.mask.template emplace<StaticMask>(srcMask);
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& dstArrayMask, DynamicMask& dstMask, const StaticMask& srcMask, std::false_type) {
			destroy(allocator, dstMask);
			dstArrayMask.mask.template emplace<StaticMask>(srcMask);
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, auto&, const DynamicMask& srcMask, auto) {
			arrayMask.mask.template emplace<DynamicMask>(cloneDynamic(allocator, srcMask).release());
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, const DynamicMask& srcMask, std::false_type) {
			if (const size_type size{dstMask.size()}; dstMask.size() == srcMask.size()) {
				for (size_type i{0U}; i != size; ++i) {
					dstMask[i] = srcMask[i];
				}
			} else {
				destroy(allocator, dstMask);
				arrayMask.mask.template emplace<DynamicMask>(cloneDynamic(allocator, srcMask).release());
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

		constexpr static void move(allocator_type& allocator, ArrayMask& dstArrayMask, DynamicMask& dstMask, StaticMask&& srcMask, std::false_type) {
			destroy(allocator, dstMask);
			dstArrayMask.mask.template emplace<StaticMask>(std::move(srcMask));
		}

		constexpr static void move(allocator_type&, ArrayMask& arrayMask, auto&, DynamicMask&& srcMask, auto) {
			arrayMask.mask.template emplace<DynamicMask>(std::move(srcMask));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, StaticMask&, DynamicMask&& srcMask, std::false_type) {
			arrayMask.mask.template emplace<DynamicMask>(cloneDynamic(allocator, std::move(srcMask)).release());
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, DynamicMask&& srcMask, std::false_type) {
			if (const size_type size{dstMask.size()}; dstMask.size() == srcMask.size()) {
				for (size_type i{0U}; i != size; ++i) {
					dstMask[i] = std::move(srcMask[i]);
				}
			} else {
				destroy(allocator, dstMask);
				arrayMask.mask.template emplace<DynamicMask>(cloneDynamic(allocator, std::move(srcMask)).release());
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
								Guard temp{cloneDynamic(leftAllocator, std::move(rightMask))};
								destroy(rightAllocator, rightMask);
								rightArrayMask.mask.template emplace<StaticMask>(std::move(leftMask));
								leftArrayMask.mask.template emplace<DynamicMask>(temp.release());
							},
							[&](DynamicMask& leftMask, StaticMask& rightMask) {
								Guard temp{cloneDynamic(rightAllocator, std::move(leftMask))};
								destroy(leftAllocator, leftMask);
								leftArrayMask.mask.template emplace<StaticMask>(std::move(rightMask));
								rightArrayMask.mask.template emplace<DynamicMask>(temp.release());
							},
							[&](DynamicMask& leftMask, DynamicMask& rightMask) {
								if (const size_type size{leftMask.size()}; leftMask.size() == rightMask.size()) {
									for (size_type i{0U}; i != size; ++i) {
										swap(leftMask[i], rightMask[i]);
									}
								} else {
									Guard leftTemp{cloneDynamic(rightAllocator, std::move(leftMask))};
									Guard rightTemp{cloneDynamic(leftAllocator, std::move(rightMask))};
									destroy(leftAllocator, leftMask);
									destroy(rightAllocator, rightMask);
									leftArrayMask.mask.template emplace<DynamicMask>(rightTemp.release());
									rightArrayMask.mask.template emplace<DynamicMask>(leftTemp.release());
								}
							}
						},
						leftArrayMask.mask,
						rightArrayMask.mask
					);
				}
			}
		}


		[[nodiscard]] constexpr bool isSet(const size_type i) const noexcept {
			return std::visit([i](const auto& mask) -> bool { return static_cast<bool>((mask[maskPos<value_type, size_type>(i)] >> maskBit<value_type, size_type>(i)) & ONE_VALUE); }, this->mask);
		}

		constexpr void set(const size_type i) noexcept {
			std::visit([i](auto& mask) { mask[maskPos<value_type, size_type>(i)] |= (ONE_VALUE << maskBit<value_type, size_type>(i)); }, this->mask);
		}

		constexpr void unset(const size_type i) noexcept {
			std::visit([i](auto& mask) { mask[maskPos<value_type, size_type>(i)] &= ~(ONE_VALUE << maskBit<value_type, size_type>(i)); }, this->mask);
		}
	};
}

#endif // ARRAY_MASK_H
