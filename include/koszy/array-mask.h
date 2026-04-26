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
#include <utility>
#include <variant>

#include "koszy/common.h"

namespace koszy::collections::mask {
	template<typename T> requires std::integral<T>
	consteval std::size_t bits() {
		return static_cast<std::size_t>(std::numeric_limits<T>::digits);
	}

	template<typename T>
	concept MaskType = std::has_single_bit(bits<T>());

	template<MaskType T>
	consteval std::size_t shifts() {
		return static_cast<std::size_t>(std::countr_zero(bits<T>()));
	}

	template<MaskType T>
	constexpr std::size_t maskPos(const std::size_t n) {
		return n >> shifts<T>();
	}

	template<MaskType T>
	constexpr std::size_t maskBit(const std::size_t n) {
		return n & (bits<T>() - ONE);
	}

	template<MaskType T>
	constexpr std::size_t maskSize(const std::size_t n) {
		return maskPos<T>(n) + static_cast<std::size_t>(static_cast<bool>(maskBit<T>(n)));
	}

	template<typename T, typename A>
	struct Deleter {
		std::reference_wrapper<A> allocator;
		std::size_t size;

		Deleter(A& allocator, const std::size_t n) : allocator{allocator}, size{n} {}

		constexpr void operator()(T* const pointer) {
			for (std::size_t i{ZERO}; i != this->size; ++i) {
				std::allocator_traits<A>::destroy(this->allocator.get(), std::addressof(pointer[i]));
			}
			std::allocator_traits<A>::deallocate(this->allocator.get(), pointer, this->size);
		}
	};

	template<MaskType T, typename A=std::allocator<T>>
	class ArrayMask {
		using allocator_type = A;
		using value_type = T;

		constexpr static T ZERO_T{0U};
		constexpr static T ONE_T{1U};

		using DynamicMask = std::unique_ptr<T[], Deleter<T, A>>;

		constexpr static std::size_t N{std::max(sizeof(DynamicMask) / sizeof(T), ONE)};
		using StaticMask = std::array<T, N>;


		constexpr static std::variant<StaticMask, DynamicMask> makeMask(A& allocator, const std::size_t n) {
			const std::size_t size{maskSize<T>(n)};
			if (size <= N) {
				return std::variant<StaticMask, DynamicMask>{StaticMask{}};
			} else {
				T* const mask{std::allocator_traits<A>::allocate(allocator, size)};
				for (std::size_t i{ZERO}; i != size; ++i) {
					std::allocator_traits<A>::construct(allocator, std::addressof(mask[i]), ZERO_T);
				}
				return std::variant<StaticMask, DynamicMask>{DynamicMask{mask, Deleter<T, A>{allocator, size}}};
			}
		}


		constexpr static std::variant<StaticMask, DynamicMask> copyMask(A&, const StaticMask& other) {
			return std::variant<StaticMask, DynamicMask>{StaticMask{other}};
		}

		constexpr static std::variant<StaticMask, DynamicMask> copyMask(A& allocator, const DynamicMask& other) {
			const std::size_t size{other.get_deleter().size};
			T* const mask{std::allocator_traits<A>::allocate(allocator, size)};
			for (std::size_t i{ZERO}; i != size; ++i) {
				std::allocator_traits<A>::construct(allocator, std::addressof(mask[i]), other[i]);
			}
			return std::variant<StaticMask, DynamicMask>{DynamicMask{mask, Deleter<T, A>{allocator, size}}};
		}

		constexpr static std::variant<StaticMask, DynamicMask> copyMask(A& allocator, const std::variant<StaticMask, DynamicMask>& other) {
			return std::visit([&](const auto& otherMask) -> std::variant<StaticMask, DynamicMask> { return copyMask(allocator, otherMask); }, other);
		}


		template<bool = true>
		constexpr static std::variant<StaticMask, DynamicMask> moveMask(A&, StaticMask&& other) {
			return std::variant<StaticMask, DynamicMask>{StaticMask{std::move(other)}};
		}

		template<bool Move>
		constexpr static std::variant<StaticMask, DynamicMask> moveMask(A& allocator, DynamicMask&& other) {
			if constexpr (Move) {
				return std::variant<StaticMask, DynamicMask>{DynamicMask{other.release(), Deleter<T, A>{allocator, other.get_deleter().size}}};
			} else {
				const std::size_t size{other.get_deleter().size};
				T* const mask{std::allocator_traits<A>::allocate(allocator, size)};
				for (std::size_t i{ZERO}; i != size; ++i) {
					std::allocator_traits<A>::construct(allocator, std::addressof(mask[i]), std::move(other[i]));
				}
				return std::variant<StaticMask, DynamicMask>{DynamicMask{mask, Deleter<T, A>{allocator, size}}};
			}
		}

		template<bool Move>
		constexpr static std::variant<StaticMask, DynamicMask> moveMask(A& allocator, std::variant<StaticMask, DynamicMask>&& other) {
			return std::visit([&](auto&& otherMask) -> std::variant<StaticMask, DynamicMask> { return moveMask<Move>(allocator, std::move(otherMask)); }, std::move(other));
		}

		public:
			constexpr ArrayMask() : allocator_{}, mask_{makeMask(this->allocator_, ZERO)} {}

			constexpr ArrayMask(const std::size_t n) : allocator_{}, mask_{makeMask(this->allocator_, n)} {}

			constexpr ArrayMask(const A& allocator) : allocator_{allocator}, mask_{makeMask(this->allocator_, ZERO)} {}

			constexpr ArrayMask(const std::size_t n, const A& allocator) : allocator_{allocator}, mask_{makeMask(this->allocator_, n)} {}

			constexpr ArrayMask(const ArrayMask<T, A>& other) : allocator_{std::allocator_traits<A>::select_on_container_copy_construction(other.allocator_)}, mask_{copyMask(this->allocator_, other.mask_)} {}

			constexpr ArrayMask(ArrayMask<T, A>&& other) noexcept : allocator_{std::move(other.allocator_)}, mask_{moveMask<true>(this->allocator_, std::move(other.mask_))} {}


			constexpr ArrayMask<T, A>& operator=(const ArrayMask<T, A>& other) {
				std::visit(
					Visitor{
						[&](StaticMask&, const StaticMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								this->allocator_ = other.allocator_;
							}
							this->mask_ = copyMask(this->allocator_, otherMask);
						},
						[&](StaticMask&, const DynamicMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								this->allocator_ = other.allocator_;
							}
							this->mask_ = copyMask(this->allocator_, otherMask);
						},
						[&](DynamicMask&, const StaticMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								std::destroy_at(std::addressof(this->mask_));
								this->allocator_ = other.allocator_;
								std::construct_at(std::addressof(this->mask_), copyMask(this->allocator_, otherMask));
							} else {
								this->mask_ = copyMask(this->allocator_, otherMask);
							}
						},
						[&](DynamicMask& thisMask, const DynamicMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								std::destroy_at(std::addressof(this->mask_));
								this->allocator_ = other.allocator_;
								std::construct_at(std::addressof(this->mask_), copyMask(this->allocator_, otherMask));
							} else {
								const std::size_t thisSize{thisMask.get_deleter().size};
								const std::size_t otherSize{otherMask.get_deleter().size};
								if (const std::size_t size{thisSize}; thisSize == otherSize) {
									for (std::size_t i{ZERO}; i != size; ++i) {
										thisMask[i] = otherMask[i];
									}
								} else {
									this->mask_ = copyMask(this->allocator_, otherMask);
								}
							}
						}
					},
					this->mask_, other.mask_
				);
				return *this;
			}

			constexpr ArrayMask<T, A>& operator=(ArrayMask<T, A>&& other) noexcept {
				std::visit(
					Visitor{
						[&](StaticMask&, StaticMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								this->allocator_ = std::move(other.allocator_);
							}
							this->mask_ = moveMask(this->allocator_, std::move(otherMask));
						},
						[&](StaticMask&, DynamicMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								this->allocator_ = std::move(other.allocator_);
								this->mask_ = moveMask<true>(this->allocator_, std::move(otherMask));
							} else {
								this->mask_ = moveMask<std::allocator_traits<A>::is_always_equal::value>(this->allocator_, std::move(otherMask));
							}
						},
						[&](DynamicMask&, StaticMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								std::destroy_at(std::addressof(this->mask_));
								this->allocator_ = std::move(other.allocator_);
								std::construct_at(std::addressof(this->mask_), moveMask(this->allocator_, std::move(otherMask)));
							} else {
								this->mask_ = moveMask(this->allocator_, std::move(otherMask));
							}
						},
						[&](DynamicMask& thisMask, DynamicMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								std::destroy_at(std::addressof(this->mask_));
								this->allocator_ = std::move(other.allocator_);
								std::construct_at(std::addressof(this->mask_), moveMask<true>(this->allocator_, std::move(otherMask)));
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								this->mask_ = moveMask<true>(this->allocator_, std::move(otherMask));
							} else {
								const std::size_t thisSize{thisMask.get_deleter().size};
								const std::size_t otherSize{otherMask.get_deleter().size};
								if (const std::size_t size{thisSize}; thisSize == otherSize) {
									for (std::size_t i{ZERO}; i != size; ++i) {
										thisMask[i] = std::move(otherMask[i]);
									}
								} else {
									this->mask_ = moveMask<false>(this->allocator_, std::move(otherMask));
								}
							}
						}
					},
					this->mask_, std::move(other.mask_)
				);
				return *this;
			}


			constexpr ~ArrayMask() = default;


			constexpr friend void swap(ArrayMask<T, A>& a, ArrayMask<T, A>& b) noexcept {
				using std::swap;
				std::visit(
					Visitor{
						[&](StaticMask&& aMask, StaticMask&& bMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
								swap(a.allocator_, b.allocator_);
							}
							swap(aMask, bMask);
						},
						[&](StaticMask&& aMask, DynamicMask&& bMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
								const std::size_t size{bMask.get_deleter().size};
								T* const temp{bMask.release()};
								swap(a.allocator_, b.allocator_);
								b.mask_ = std::variant<StaticMask, DynamicMask>{StaticMask{std::move(aMask)}};
								a.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{temp, Deleter<T, A>{a.allocator_, size}}};
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								const std::size_t size{bMask.get_deleter().size};
								T* const temp{bMask.release()};
								b.mask_ = std::variant<StaticMask, DynamicMask>{StaticMask{std::move(aMask)}};
								a.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{temp, Deleter<T, A>{a.allocator_, size}}};
							} else {
								const std::size_t size{bMask.get_deleter().size};
								T* const temp{std::allocator_traits<A>::allocate(a.allocator_, size)};
								for (std::size_t i{ZERO}; i != size; ++i) {
									std::allocator_traits<A>::construct(a.allocator_, std::addressof(temp[i]), std::move(bMask[i]));
								}
								b.mask_ = std::variant<StaticMask, DynamicMask>{StaticMask{std::move(aMask)}};
								a.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{temp, Deleter<T, A>{a.allocator_, size}}};
							}
						},
						[&](DynamicMask&& aMask, StaticMask&& bMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
								const std::size_t size{aMask.get_deleter().size};
								T* const temp{aMask.release()};
								swap(a.allocator_, b.allocator_);
								a.mask_ = std::variant<StaticMask, DynamicMask>{StaticMask{std::move(bMask)}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{temp, Deleter<T, A>{b.allocator_, size}}};
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								const std::size_t size{aMask.get_deleter().size};
								T* const temp{aMask.release()};
								a.mask_ = std::variant<StaticMask, DynamicMask>{StaticMask{std::move(bMask)}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{temp, Deleter<T, A>{b.allocator_, size}}};
							} else {
								const std::size_t size{aMask.get_deleter().size};
								T* const temp{std::allocator_traits<A>::allocate(b.allocator_, size)};
								for (std::size_t i{ZERO}; i != size; ++i) {
									std::allocator_traits<A>::construct(b.allocator_, std::addressof(temp[i]), std::move(aMask[i]));
								}
								a.mask_ = std::variant<StaticMask, DynamicMask>{StaticMask{std::move(bMask)}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{temp, Deleter<T, A>{b.allocator_, size}}};
							}
						},
						[&](DynamicMask&& aMask, DynamicMask&& bMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
								const std::size_t aSize{aMask.get_deleter().size};
								const std::size_t bSize{bMask.get_deleter().size};
								T* const aTemp{aMask.release()};
								T* const bTemp{bMask.release()};
								swap(a.allocator_, b.allocator_);
								a.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{bTemp, Deleter<T, A>{a.allocator_, bSize}}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{aTemp, Deleter<T, A>{b.allocator_, aSize}}};
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								const std::size_t aSize{aMask.get_deleter().size};
								const std::size_t bSize{bMask.get_deleter().size};
								T* const aTemp{aMask.release()};
								T* const bTemp{bMask.release()};
								a.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{bTemp, Deleter<T, A>{a.allocator_, bSize}}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{aTemp, Deleter<T, A>{b.allocator_, aSize}}};
							} else {
								const std::size_t aSize{aMask.get_deleter().size};
								const std::size_t bSize{bMask.get_deleter().size};
								if (const std::size_t size{aSize}; aSize == bSize) {
									for (std::size_t i{ZERO}; i != size; ++i) {
										swap(aMask[i], bMask[i]);
									}
								} else {
									T* const aTemp{std::allocator_traits<A>::allocate(b.allocator_, aSize)};
									T* const bTemp{std::allocator_traits<A>::allocate(a.allocator_, bSize)};
									for (std::size_t i{ZERO}; i != std::max(aSize, bSize); ++i) {
										if (i < aSize) {
											std::allocator_traits<A>::construct(b.allocator_, std::addressof(aTemp[i]), std::move(aMask[i]));
										}
										if (i < bSize) {
											std::allocator_traits<A>::construct(a.allocator_, std::addressof(bTemp[i]), std::move(bMask[i]));
										}
									}
									a.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{bTemp, Deleter<T, A>{a.allocator_, bSize}}};
									b.mask_ = std::variant<StaticMask, DynamicMask>{DynamicMask{aTemp, Deleter<T, A>{b.allocator_, aSize}}};
								}
							}
						}
					},
					std::move(a.mask_), std::move(b.mask_)
				);
			}


			[[nodiscard]] constexpr bool isSet(const std::size_t n) const {
				return std::visit([n](const auto& mask) -> bool { return static_cast<bool>((mask[maskPos<T>(n)] >> maskBit<T>(n)) & ONE_T); }, this->mask_);
			}

			constexpr void set(const std::size_t i) {
				std::visit([i](auto& mask) { mask[maskPos<T>(i)] |= (ONE_T << maskBit<T>(i)); }, this->mask_);
			}

			constexpr void unset(const std::size_t i) {
				std::visit([i](auto& mask) { mask[maskPos<T>(i)] &= ~(ONE_T << maskBit<T>(i)); }, this->mask_);
			}

			constexpr void reset(const std::size_t i) {
				this->mask_ = makeMask(this->allocator_, i);
			}

		private:
			[[no_unique_address]] A allocator_;
			std::variant<StaticMask, DynamicMask> mask_;
	};
}

#endif // ARRAY_MASK_H
