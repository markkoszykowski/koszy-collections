#ifndef HASH_MASK_H
#define HASH_MASK_H

#include <array>
#include <bit>
#include <concepts>
#include <functional>
#include <limits>
#include <memory>
#include <variant>

namespace koszy::collections::hash::mask {
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
		return n & static_cast<std::size_t>(bits<T>() - 1U);
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

		void operator()(T* const pointer) {
			for (std::size_t i{0U}; i != this->size; ++i) {
				std::allocator_traits<A>::destroy(this->allocator.get(), std::addressof(pointer[i]));
			}
			std::allocator_traits<A>::deallocate(this->allocator.get(), pointer, this->size);
		}
	};

	template<typename... Ts>
	struct Overloaded : Ts... {
		using Ts::operator()...;
	};

	template<MaskType T, typename A=std::allocator<T>>
	class HashMask {
		using DynamicMask = std::unique_ptr<T[], Deleter<T, A>>;

		constexpr static std::size_t N{std::max(static_cast<std::size_t>(sizeof(DynamicMask) / sizeof(T)), static_cast<std::size_t>(1U))};
		using StaticMask = std::array<T, N>;

		static std::variant<StaticMask, DynamicMask> makeMask(A& allocator, const std::size_t n) {
			const std::size_t size{maskSize<T>(n)};
			if (size <= N) {
				return std::variant<StaticMask, DynamicMask>{StaticMask{}};
			} else {
				T* const mask{std::allocator_traits<A>::allocate(allocator, size)};
				for (std::size_t i{0U}; i != size; ++i) {
					std::allocator_traits<A>::construct(allocator, std::addressof(mask[i]));
				}
				return std::variant<StaticMask, DynamicMask>{DynamicMask{mask, Deleter<T, A>{allocator, size}}};
			}
		}

		static std::variant<StaticMask, DynamicMask> copyMask(A& allocator, const std::variant<StaticMask, DynamicMask>& other) {
			return std::visit(
				Overloaded{
					[](const StaticMask& other) -> std::variant<StaticMask, DynamicMask> { return std::variant<StaticMask, DynamicMask>{StaticMask{other}}; },
					[&allocator](const DynamicMask& other) -> std::variant<StaticMask, DynamicMask> {
						const std::size_t size{other.get_deleter().size};
						T* const mask{std::allocator_traits<A>::allocate(allocator, size)};
						for (std::size_t i{0U}; i != size; ++i) {
							std::allocator_traits<A>::construct(allocator, std::addressof(mask[i]), other[i]);
						}
						return std::variant<StaticMask, DynamicMask>{DynamicMask{mask, Deleter<T, A>{allocator, size}}};
					}
				},
				other
			);
		}

		template<bool Move>
		static std::variant<StaticMask, DynamicMask> moveMask(A& allocator, std::variant<StaticMask, DynamicMask>&& other) {
			return std::visit(
				Overloaded{
					[](StaticMask&& other) -> std::variant<StaticMask, DynamicMask> { return std::variant<StaticMask, DynamicMask>{StaticMask{std::move(other)}}; },
					[&allocator](DynamicMask&& other) -> std::variant<StaticMask, DynamicMask> {
						const std::size_t size{other.get_deleter().size};
						if constexpr (Move) {
							return std::variant<StaticMask, DynamicMask>{DynamicMask{other.release(), Deleter<T, A>{allocator, size}}};
						} else {
							T* const mask{std::allocator_traits<A>::allocate(allocator, size)};
							for (std::size_t i{0U}; i != size; ++i) {
								std::allocator_traits<A>::construct(allocator, std::addressof(mask[i]), std::move(other[i]));
							}
							return std::variant<StaticMask, DynamicMask>{DynamicMask{mask, Deleter<T, A>{allocator, size}}};
						}
					}
				},
				std::move(other)
			);
		}

		public:
			HashMask() : allocator_{}, mask_{makeMask(this->allocator_, 0U)} {}

			HashMask(const std::size_t n) : allocator_{}, mask_{makeMask(this->allocator_, n)} {}

			HashMask(const A& allocator) : allocator_{allocator}, mask_{makeMask(this->allocator_, 0U)} {}

			HashMask(const std::size_t n, const A& allocator) : allocator_{allocator}, mask_{makeMask(this->allocator_, n)} {}

			HashMask(const HashMask& other) : allocator_{std::allocator_traits<A>::select_on_container_copy_construction(other.allocator_)}, mask_{copyMask(this->allocator_, other.mask_)} {}

			HashMask(HashMask&& other) : allocator_{std::move(other.allocator_)}, mask_{moveMask<true>(this->allocator_, std::move(other.mask_))} {}


			HashMask& operator=(const HashMask& other) {
				if constexpr (std::allocator_traits<A>::propagate_on_container_copy_assignment::value) {
					std::destroy_at(std::addressof(this->mask_));
					this->allocator_ = other.allocator_;
					std::construct_at(std::addressof(this->mask_), copyMask(this->allocator_, other.mask_));
				} else {
					this->mask_ = copyMask(this->allocator_, other.mask_);
				}
				return *this;
			}

			HashMask& operator=(HashMask&& other) {
				if constexpr (std::allocator_traits<A>::propagate_on_container_move_assignment::value) {
					std::destroy_at(std::addressof(this->mask_));
					this->allocator_ = std::move(other.allocator_);
					std::construct_at(std::addressof(this->mask_), moveMask<true>(this->allocator_, std::move(other.mask_)));
				} else {
					this->mask_ = moveMask<std::allocator_traits<A>::is_always_equal::value>(this->allocator_, std::move(other.mask_));
				}
				return *this;
			}


			~HashMask() = default;


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
				this->mask_ = makeMask(this->allocator_, n);
			}

		private:
			[[no_unique_address]] A allocator_;
			std::variant<StaticMask, DynamicMask> mask_;
	};
}

#endif // HASH_MASK_H
