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

	template<MaskType T, typename A=std::allocator<T>>
	class ArrayMask {
		using allocator_type = A;
		using value_type = T;

		constexpr static T ZERO_T{0U};
		constexpr static T ONE_T{1U};

		struct Deleter;
		using DynamicMask = std::unique_ptr<T[], Deleter>;

		constexpr static std::size_t N{std::max(sizeof(DynamicMask) / sizeof(T), ONE)};
		using StaticMask = std::array<T, N>;


		constexpr static std::variant<StaticMask, DynamicMask> makeMask(ArrayMask<T, A>& mask, const std::size_t n) {
			const std::size_t size{maskSize<T>(n)};
			if (size <= N) {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>};
			} else {
				T* const pointer{std::allocator_traits<A>::allocate(mask.allocator_, size)};
				for (std::size_t i{ZERO}; i != size; ++i) {
					std::allocator_traits<A>::construct(mask.allocator_, std::addressof(pointer[i]), ZERO_T);
				}
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, pointer, Deleter{mask, size}};
			}
		}


		constexpr static std::variant<StaticMask, DynamicMask> copyMask(ArrayMask<T, A>&, const StaticMask& other) {
			return std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, other};
		}

		constexpr static std::variant<StaticMask, DynamicMask> copyMask(ArrayMask<T, A>& mask, const DynamicMask& other) {
			const std::size_t size{other.get_deleter().size};
			T* const pointer{std::allocator_traits<A>::allocate(mask.allocator_, size)};
			for (std::size_t i{ZERO}; i != size; ++i) {
				std::allocator_traits<A>::construct(mask.allocator_, std::addressof(pointer[i]), other[i]);
			}
			return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, pointer, Deleter{mask, size}};
		}

		constexpr static std::variant<StaticMask, DynamicMask> copyMask(ArrayMask<T, A>& mask, const std::variant<StaticMask, DynamicMask>& other) {
			return std::visit([&](const auto& otherMask) -> std::variant<StaticMask, DynamicMask> { return copyMask(mask, otherMask); }, other);
		}


		template<bool = true>
		constexpr static std::variant<StaticMask, DynamicMask> moveMask(ArrayMask<T, A>&, StaticMask&& other) {
			return std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(other)};
		}

		template<bool Move>
		constexpr static std::variant<StaticMask, DynamicMask> moveMask(ArrayMask<T, A>& mask, DynamicMask&& other) {
			if constexpr (Move) {
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, other.release(), Deleter{mask, other.get_deleter().size}};
			} else {
				const std::size_t size{other.get_deleter().size};
				T* const pointer{std::allocator_traits<A>::allocate(mask.allocator_, size)};
				for (std::size_t i{ZERO}; i != size; ++i) {
					std::allocator_traits<A>::construct(mask.allocator_, std::addressof(pointer[i]), std::move(other[i]));
				}
				return std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, pointer, Deleter{mask, size}};
			}
		}

		template<bool Move>
		constexpr static std::variant<StaticMask, DynamicMask> moveMask(ArrayMask<T, A>& mask, std::variant<StaticMask, DynamicMask>&& other) {
			return std::visit([&](auto&& otherMask) -> std::variant<StaticMask, DynamicMask> { return moveMask<Move>(mask, std::move(otherMask)); }, std::move(other));
		}


		struct Deleter {
			std::reference_wrapper<ArrayMask<T, A>> mask;
			std::size_t size;

			constexpr Deleter(ArrayMask<T, A>& mask, const std::size_t n) : mask{mask}, size{n} {}

			constexpr void operator()(T* const pointer) {
				for (std::size_t i{ZERO}; i != this->size; ++i) {
					std::allocator_traits<A>::destroy(this->mask.get().allocator_, std::addressof(pointer[i]));
				}
				std::allocator_traits<A>::deallocate(this->mask.get().allocator_, pointer, this->size);
			}
		};

		public:
			constexpr ArrayMask() : allocator_{}, mask_{makeMask(*this, ZERO)} {}

			constexpr ArrayMask(const std::size_t n) : allocator_{}, mask_{makeMask(*this, n)} {}

			constexpr ArrayMask(const A& allocator) : allocator_{allocator}, mask_{makeMask(*this, ZERO)} {}

			constexpr ArrayMask(const std::size_t n, const A& allocator) : allocator_{allocator}, mask_{makeMask(*this, n)} {}

			constexpr ArrayMask(const ArrayMask<T, A>& other) : allocator_{std::allocator_traits<A>::select_on_container_copy_construction(other.allocator_)}, mask_{copyMask(*this, other.mask_)} {}

			constexpr ArrayMask(ArrayMask<T, A>&& other) noexcept : allocator_{std::move(other.allocator_)}, mask_{moveMask<true>(*this, std::move(other.mask_))} {}


			constexpr ArrayMask<T, A>& operator=(const ArrayMask<T, A>& other) {
				std::visit(
					Visitor{
						[&](StaticMask&, const StaticMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								this->allocator_ = other.allocator_;
							}
							this->mask_ = copyMask(*this, otherMask);
						},
						[&](StaticMask&, const DynamicMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								this->allocator_ = other.allocator_;
							}
							this->mask_ = copyMask(*this, otherMask);
						},
						[&](DynamicMask& thisMask, const StaticMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								thisMask.reset();
								this->allocator_ = other.allocator_;
								this->mask_ = copyMask(*this, otherMask);
							} else {
								this->mask_ = copyMask(*this, otherMask);
							}
						},
						[&](DynamicMask& thisMask, const DynamicMask& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
								thisMask.reset();
								this->allocator_ = other.allocator_;
								this->mask_ = copyMask(*this, otherMask);
							} else {
								const std::size_t thisSize{thisMask.get_deleter().size};
								const std::size_t otherSize{otherMask.get_deleter().size};
								if (const std::size_t size{thisSize}; thisSize == otherSize) {
									for (std::size_t i{ZERO}; i != size; ++i) {
										thisMask[i] = otherMask[i];
									}
								} else {
									this->mask_ = copyMask(*this, otherMask);
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
							this->mask_ = moveMask(*this, std::move(otherMask));
						},
						[&](StaticMask&, DynamicMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								this->allocator_ = std::move(other.allocator_);
								this->mask_ = moveMask<true>(*this, std::move(otherMask));
							} else {
								this->mask_ = moveMask<std::allocator_traits<A>::is_always_equal::value>(*this, std::move(otherMask));
							}
						},
						[&](DynamicMask& thisMask, StaticMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								thisMask.reset();
								this->allocator_ = std::move(other.allocator_);
								this->mask_ = moveMask(*this, std::move(otherMask));
							} else {
								this->mask_ = moveMask(*this, std::move(otherMask));
							}
						},
						[&](DynamicMask& thisMask, DynamicMask&& otherMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
								thisMask.reset();
								this->allocator_ = std::move(other.allocator_);
								this->mask_ = moveMask<true>(*this, std::move(otherMask));
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								this->mask_ = moveMask<true>(*this, std::move(otherMask));
							} else {
								const std::size_t thisSize{thisMask.get_deleter().size};
								const std::size_t otherSize{otherMask.get_deleter().size};
								if (const std::size_t size{thisSize}; thisSize == otherSize) {
									for (std::size_t i{ZERO}; i != size; ++i) {
										thisMask[i] = std::move(otherMask[i]);
									}
								} else {
									this->mask_ = moveMask<false>(*this, std::move(otherMask));
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
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(aMask)};
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, temp, Deleter{a, size}};
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								const std::size_t size{bMask.get_deleter().size};
								T* const temp{bMask.release()};
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(aMask)};
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, temp, Deleter{a, size}};
							} else {
								const std::size_t size{bMask.get_deleter().size};
								T* const temp{std::allocator_traits<A>::allocate(a.allocator_, size)};
								for (std::size_t i{ZERO}; i != size; ++i) {
									std::allocator_traits<A>::construct(a.allocator_, std::addressof(temp[i]), std::move(bMask[i]));
								}
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(aMask)};
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, temp, Deleter{a, size}};
							}
						},
						[&](DynamicMask&& aMask, StaticMask&& bMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
								const std::size_t size{aMask.get_deleter().size};
								T* const temp{aMask.release()};
								swap(a.allocator_, b.allocator_);
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(bMask)};
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, temp, Deleter{b, size}};
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								const std::size_t size{aMask.get_deleter().size};
								T* const temp{aMask.release()};
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(bMask)};
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, temp, Deleter{b, size}};
							} else {
								const std::size_t size{aMask.get_deleter().size};
								T* const temp{std::allocator_traits<A>::allocate(b.allocator_, size)};
								for (std::size_t i{ZERO}; i != size; ++i) {
									std::allocator_traits<A>::construct(b.allocator_, std::addressof(temp[i]), std::move(aMask[i]));
								}
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<StaticMask>, std::move(bMask)};
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, temp, Deleter{b, size}};
							}
						},
						[&](DynamicMask&& aMask, DynamicMask&& bMask) {
							if constexpr (std::allocator_traits<A>::propagate_on_container_swap::value) {
								const std::size_t aSize{aMask.get_deleter().size};
								const std::size_t bSize{bMask.get_deleter().size};
								T* const aTemp{aMask.release()};
								T* const bTemp{bMask.release()};
								swap(a.allocator_, b.allocator_);
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, bTemp, Deleter{a, bSize}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, aTemp, Deleter{b, aSize}};
							} else if constexpr (std::allocator_traits<A>::is_always_equal::value) {
								const std::size_t aSize{aMask.get_deleter().size};
								const std::size_t bSize{bMask.get_deleter().size};
								T* const aTemp{aMask.release()};
								T* const bTemp{bMask.release()};
								a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, bTemp, Deleter{a, bSize}};
								b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, aTemp, Deleter{b, aSize}};
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
									a.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, bTemp, Deleter{a, bSize}};
									b.mask_ = std::variant<StaticMask, DynamicMask>{std::in_place_type<DynamicMask>, aTemp, Deleter{b, aSize}};
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
				this->mask_ = makeMask(*this, i);
			}

		private:
			[[no_unique_address]] A allocator_;
			std::variant<StaticMask, DynamicMask> mask_;
	};
}

#endif // ARRAY_MASK_H
