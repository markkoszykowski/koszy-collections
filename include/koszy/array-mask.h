#ifndef ARRAY_MASK_H
#define ARRAY_MASK_H

#include <algorithm>
#include <array>
#include <bit>
#include <concepts>
#include <cstdint>
#include <functional>
#include <limits>
#include <memory>
#include <type_traits>
#include <utility>
#include <variant>

#include "koszy/common.h"

namespace koszy::collections::mask {
	template <std::integral T>
	consteval std::size_t bits() noexcept {
		return static_cast<std::size_t>(std::numeric_limits<T>::digits);
	}

	template <typename T>
	concept MaskType = std::is_trivial_v<T> && std::has_single_bit(bits<T>());

	template <MaskType T, std::unsigned_integral S=std::size_t>
	consteval S shifts() noexcept {
		return static_cast<S>(std::countr_zero(bits<T>()));
	}

	template <MaskType T, std::unsigned_integral S=std::size_t>
	constexpr S maskPos(const S n) noexcept {
		return n >> shifts<T, S>();
	}

	template <MaskType T, std::unsigned_integral S=std::size_t>
	constexpr S maskBit(const S n) noexcept {
		return n & (static_cast<S>(bits<T>()) - 1U);
	}

	template <MaskType T, std::unsigned_integral S=std::size_t>
	constexpr S maskSize(const S n) noexcept {
		return maskPos<T, S>(n) + static_cast<S>(static_cast<bool>(maskBit<T, S>(n)));
	}

	template <MaskType T, typename A=std::allocator<T>>
	struct ArrayMask {
		using value_type = T;
		using allocator_type = A;
		using size_type = std::allocator_traits<allocator_type>::size_type;
		using difference_type = std::allocator_traits<allocator_type>::difference_type;
		using reference = value_type&;
		using const_reference = const value_type&;
		using pointer = std::allocator_traits<allocator_type>::pointer;
		using const_pointer = std::allocator_traits<allocator_type>::const_pointer;

		constexpr static value_type ZERO_VALUE{0U};
		constexpr static value_type ONE_VALUE{1U};

		struct DynamicMask {
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
		};

		constexpr static std::size_t N{std::max(sizeof(DynamicMask) / sizeof(value_type), ONE)};

		struct StaticMask {
			std::array<value_type, N> mask;

			[[nodiscard]] constexpr std::array<value_type, N>::pointer data() & noexcept {
				return this->mask.data();
			}

			[[nodiscard]] constexpr std::array<value_type, N>::const_pointer data() const & noexcept {
				return this->mask.data();
			}

			[[nodiscard]] constexpr std::array<value_type, N>::size_type size() const & noexcept {
				return this->mask.size();
			}

			template <typename Self>
			[[nodiscard]] constexpr like_t<Self, value_type> operator[](this Self&& self, const size_type i) noexcept {
				return std::forward_like<Self>(self.mask[i]);
			}
		};


		std::variant<StaticMask, DynamicMask> mask;


		[[nodiscard]] constexpr bool isSet(const size_type i) const noexcept {
			return std::visit([i](const auto& mask) -> bool { return static_cast<bool>((mask[maskPos<value_type, size_type>(i)] >> maskBit<value_type, size_type>(i)) & ONE_VALUE); }, this->mask);
		}

		constexpr void set(const size_type i) noexcept {
			std::visit([i](auto& mask) { mask[maskPos<value_type, size_type>(i)] |= (ONE_VALUE << maskBit<value_type, size_type>(i)); }, this->mask);
		}

		constexpr void unset(const size_type i) noexcept {
			std::visit([i](auto& mask) { mask[maskPos<value_type, size_type>(i)] &= ~(ONE_VALUE << maskBit<value_type, size_type>(i)); }, this->mask);
		}


		constexpr static void destroy(allocator_type&, StaticMask&) noexcept(std::is_nothrow_destructible_v<value_type>) {
		}

		constexpr static void destroy(allocator_type& allocator, const pointer data, const size_type size, const size_type len) noexcept(std::is_nothrow_destructible_v<value_type>) {
			if (data != nullptr) {
				if constexpr (!std::is_trivially_destructible_v<value_type>) {
					for (size_type i{0U}; i != len; ++i) {
						std::allocator_traits<allocator_type>::destroy(allocator, std::to_address(data + i));
					}
				}
				std::allocator_traits<allocator_type>::deallocate(allocator, data, size);
			}
		}

		constexpr static void destroy(allocator_type& allocator, const pointer data, const size_type size) noexcept(std::is_nothrow_destructible_v<value_type>) {
			destroy(allocator, data, size, size);
		}

		constexpr static void destroy(allocator_type& allocator, DynamicMask& mask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			destroy(allocator, mask.data(), mask.size());
		}

		constexpr static void destroy(allocator_type& allocator, ArrayMask& arrayMask) noexcept(std::is_nothrow_destructible_v<value_type>) {
			std::visit([&](auto& mask) { destroy(allocator, mask); }, arrayMask.mask);
		}


		struct Guard {
			std::reference_wrapper<allocator_type> allocator;
			pointer data;
			size_type size;
			size_type len;

			constexpr Guard(allocator_type& allocator, const size_type size) : allocator{allocator},
				data{std::allocator_traits<allocator_type>::allocate(this->allocator.get(), size)},
				size{size},
				len{0U}
			{}

			constexpr Guard(const Guard&) = delete;

			constexpr Guard(Guard&& other) noexcept : allocator{std::move(other.allocator)},
				data{std::exchange(other.data, nullptr)},
				size{std::exchange(other.size, 0U)},
				len{std::exchange(other.len, 0U)}
			{}

			constexpr Guard& operator=(const Guard&) = delete;

			constexpr Guard& operator=(Guard&& other) noexcept {
				this->allocator = std::move(other.allocator);
				this->data = std::exchange(other.data, nullptr);
				this->size = std::exchange(other.size, 0U);
				this->len = std::exchange(other.len, 0U);
				return *this;
			}

			constexpr ~Guard() noexcept {
				destroy(this->allocator.get(), this->data, this->size, this->len);
			}

			[[nodiscard]] constexpr DynamicMask release() noexcept {
				const pointer data{std::exchange(this->data, nullptr)};
				const size_type size{std::exchange(this->size, 0U)};
				const size_type len{std::exchange(this->len, 0U)};
				return DynamicMask{data, data + size};
			}
		};


		template <typename Dynamic> requires std::is_same_v<std::remove_cvref_t<Dynamic>, DynamicMask>
		[[nodiscard]] constexpr static Guard guard(allocator_type& allocator, Dynamic&& other) {
			Guard guard{allocator, other.size()};
			for (; guard.len != guard.size; ++guard.len) {
				std::allocator_traits<allocator_type>::construct(allocator, std::to_address(guard.data + guard.len), std::forward<Dynamic>(other)[guard.len]);
			}
			return guard;
		}

		template <typename Static> requires std::is_same_v<std::remove_cvref_t<Static>, StaticMask>
		[[nodiscard]] constexpr static StaticMask clone(allocator_type&, Static&& other) {
			return StaticMask{std::forward<Static>(other)};
		}

		template <typename Dynamic> requires std::is_same_v<std::remove_cvref_t<Dynamic>, DynamicMask>
		[[nodiscard]] constexpr static DynamicMask clone(allocator_type& allocator, Dynamic&& other) {
			return guard(allocator, std::forward<Dynamic>(other)).release();
		}

		[[nodiscard]] constexpr static StaticMask move(StaticMask&& other) {
			return StaticMask{std::move(other)};
		}

		[[nodiscard]] constexpr static DynamicMask move(DynamicMask&& other) {
			return DynamicMask{std::exchange(other.begin, nullptr), std::exchange(other.end, nullptr)};
		}


		[[nodiscard]] constexpr static std::variant<StaticMask, DynamicMask> construct(allocator_type& allocator, const size_type n) {
			const size_type size{maskSize<value_type, size_type>(n)};
			if (size <= N) {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>};
			} else {
				Guard guard{allocator, size};
				for (; guard.len != guard.size; ++guard.len) {
					std::allocator_traits<allocator_type>::construct(allocator, std::to_address(guard.data + guard.len), ZERO_VALUE);
				}
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, guard.release()};
			}
		}


		constexpr explicit ArrayMask(allocator_type& allocator) : mask{construct(allocator, 0U)} {}

		constexpr explicit ArrayMask(allocator_type& allocator, const size_type n) : mask{construct(allocator, n)} {}

		constexpr explicit ArrayMask(StaticMask mask) : mask{std::in_place_type<StaticMask>, std::move(mask)} {}

		constexpr explicit ArrayMask(DynamicMask mask) : mask{std::in_place_type<DynamicMask>, std::move(mask)} {}


		constexpr static ArrayMask copy(allocator_type& allocator, const ArrayMask& arrayMask) {
			return std::visit([&](const auto& mask) -> ArrayMask { return ArrayMask{clone(allocator, mask)}; }, arrayMask.mask);
		}

		template <typename Mask>
		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, auto&, const Mask& srcMask, auto) {
			arrayMask.mask.template emplace<Mask>(clone(allocator, srcMask));
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, const StaticMask& srcMask, std::false_type) {
			destroy(allocator, dstMask);
			arrayMask.mask.template emplace<StaticMask>(clone(allocator, srcMask));
		}

		constexpr static void copy(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, const DynamicMask& srcMask, std::false_type) {
			if (const size_type size{dstMask.size()}; dstMask.size() == srcMask.size()) {
				for (size_type i{0U}; i != size; ++i) {
					dstMask[i] = srcMask[i];
				}
			} else {
				destroy(allocator, dstMask);
				arrayMask.mask.template emplace<DynamicMask>(clone(allocator, srcMask));
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


		constexpr static ArrayMask move(allocator_type&, ArrayMask&& arrayMask) {
			return std::visit([&](auto&& mask) -> ArrayMask { return ArrayMask{move(std::move(mask))}; }, std::move(arrayMask.mask));
		}

		template <typename Mask>
		constexpr static void move(allocator_type&, ArrayMask& arrayMask, auto&, Mask&& srcMask, auto) {
			arrayMask.mask.template emplace<std::remove_cvref_t<Mask>>(move(std::move(srcMask)));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, StaticMask&& srcMask, std::false_type) {
			destroy(allocator, dstMask);
			arrayMask.mask.template emplace<StaticMask>(move(std::move(srcMask)));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, StaticMask&, DynamicMask&& srcMask, std::false_type) {
			arrayMask.mask.template emplace<DynamicMask>(clone(allocator, std::move(srcMask)));
		}

		constexpr static void move(allocator_type& allocator, ArrayMask& arrayMask, DynamicMask& dstMask, DynamicMask&& srcMask, std::false_type) {
			if (const size_type size{dstMask.size()}; dstMask.size() == srcMask.size()) {
				for (size_type i{0U}; i != size; ++i) {
					dstMask[i] = std::move(srcMask[i]);
				}
			} else {
				destroy(allocator, dstMask);
				arrayMask.mask.template emplace<DynamicMask>(clone(allocator, std::move(srcMask)));
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
								Guard temp{guard(leftAllocator, std::move(rightMask))};
								destroy(rightAllocator, rightMask);
								rightArrayMask.mask.template emplace<StaticMask>(std::move(leftMask));
								leftArrayMask.mask.template emplace<DynamicMask>(temp.release());
							},
							[&](DynamicMask& leftMask, StaticMask& rightMask) {
								Guard temp{guard(rightAllocator, std::move(leftMask))};
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
									Guard leftTemp{guard(rightAllocator, std::move(leftMask))};
									Guard rightTemp{guard(leftAllocator, std::move(rightMask))};
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
	};

	template <typename T, typename A=std::allocator<T>>
	struct Guard {
		std::reference_wrapper<A> allocator;
		ArrayMask<T, A> mask;

		constexpr explicit Guard(ArrayMask<T, A>::allocator_type& allocator) : allocator{allocator}, mask{allocator} {}

		constexpr explicit Guard(ArrayMask<T, A>::allocator_type& allocator, const ArrayMask<T, A>::size_type n) : allocator{allocator}, mask{allocator, n} {}

		constexpr Guard(const Guard&) = delete;

		constexpr Guard(Guard&& other) noexcept : allocator{std::move(other.allocator)}, mask{ArrayMask<T, A>::move(this->allocator.get(), std::move(other.mask))} {}

		constexpr Guard& operator=(const Guard&) = delete;

		constexpr Guard& operator=(Guard&& other) noexcept {
			this->allocator = std::move(other.allocator);
			this->mask = ArrayMask<T, A>::move(this->allocator.get(), std::move(other.mask));
			return *this;
		}

		constexpr ~Guard() noexcept {
			ArrayMask<T, A>::destroy(this->allocator.get(), this->mask);
		}

		[[nodiscard]] constexpr ArrayMask<T, A> release() noexcept {
			return ArrayMask<T, A>::move(this->allocator.get(), std::move(this->mask));
		}
	};
}

#endif // ARRAY_MASK_H
