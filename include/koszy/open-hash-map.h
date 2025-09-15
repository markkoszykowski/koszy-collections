#ifndef OPEN_HASH_MAP
#define OPEN_HASH_MAP

#include <algorithm>
#include <cstdint>
#include <functional>
#include <memory>
#include <optional>
#include <stdexcept>
#include <tuple>
#include <utility>

#include "koszy/hash-common.h"

namespace koszy::collections::hash {
	template<
		typename K,
		typename V,
		typename H = std::hash<K>,
		typename E = std::equal_to<K>,
		template<typename T> typename A=std::allocator
	>
	class OpenHashMap {
		public:
			OpenHashMap() : OpenHashMap{
				std::make_tuple(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR),
				H{},
				E{},
				A<std::pair<K, V>>{},
				A<std::uint_fast8_t>{}
			} {
			}

			explicit OpenHashMap(const std::size_t expected) : OpenHashMap{
				std::make_tuple(arraySize(expected, DEFAULT_LOAD_FACTOR), DEFAULT_LOAD_FACTOR),
				H{},
				E{},
				A<std::pair<K, V>>{},
				A<std::uint_fast8_t>{}
			} {
			}

			explicit OpenHashMap(
				const std::size_t expected,
				const float f
			) : OpenHashMap{
				std::make_tuple(arraySize(expected, f), f),
				H{},
				E{},
				A<std::pair<K, V>>{},
				A<std::uint_fast8_t>{}
			} {
			}

			explicit OpenHashMap(const H& hash, const E& eq) : OpenHashMap{
				std::make_tuple(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR),
				hash,
				eq,
				A<std::pair<K, V>>{},
				A<std::uint_fast8_t>{}
			} {
			}

			explicit OpenHashMap(
				const A<std::pair<K, V>>& map_allocator,
				const A<std::uint_fast8_t>& mask_allocator
			) : OpenHashMap{
				std::make_tuple(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR),
				H{},
				E{},
				map_allocator,
				mask_allocator
			} {
			}

			explicit OpenHashMap(
				const std::size_t expected,
				const float f,
				const H& hash,
				const E& eq,
				const A<std::pair<K, V>>& map_allocator,
				const A<std::uint_fast8_t>& mask_allocator
			) : OpenHashMap{
				std::make_tuple(arraySize(expected, f), f),
				hash,
				eq,
				map_allocator,
				mask_allocator
			} {
			}


			// OpenHashMap(const OpenHashMap& other) {
			// 	// TODO
			// };
			//
			// OpenHashMap(const OpenHashMap&& other) {
			// 	// TODO
			// };
			//
			//
			// OpenHashMap& operator=(const OpenHashMap& other) {
			// 	// TODO
			// }
			//
			// OpenHashMap& operator=(const OpenHashMap&& other) {
			// 	// TODO
			// }


			~OpenHashMap() {
				for (std::size_t pos{0U}, cnt{0U}; pos != this->n_ && cnt != this->size_; ++pos) {
					if (exists(pos, this->mask_)) {
						std::allocator_traits<A<std::pair<K, V>>>::destroy(this->map_allocator_, &this->map_[pos]);

						++cnt;
					}
				}

				std::allocator_traits<A<std::pair<K, V>>>::deallocate(this->map_allocator_, this->map_, this->n_);
				std::allocator_traits<A<std::uint_fast8_t>>::deallocate(this->mask_allocator_, this->mask_, maskSize<std::uint_fast8_t>(this->n_));
			}

			std::size_t size() const {
				return this->size_;
			}

			bool empty() const {
				return this->size_ == 0U;
			}

			std::size_t max_size() const {
				return std::allocator_traits<A<std::pair<K, V>>>::max_size(this->map_allocator_);
			}

			void clear() {
				if (this->size_ == 0U) {
					return;
				}

				for (std::size_t pos{0U}, cnt{0U}; cnt != this->size_; ++pos) {
					if (exists(pos, this->mask_)) {
						std::allocator_traits<A<std::pair<K, V>>>::destroy(this->map_allocator_, &this->map_[pos]);

						++cnt;
					}
				}

				this->size_ = 0U;
				std::fill(this->mask_, this->mask_ + maskSize<std::uint_fast8_t>(this->n_), 0U);
			}


			V& at(const K& key) {
				const std::size_t pos{this->find(key)};
				if (exists(pos, this->mask_)) {
					return this->map_[pos].second;
				} else {
					throw std::out_of_range{"OpenHashMap::at"};
				}
			}

			const V& at(const K& key) const {
				const std::size_t pos{this->find(key)};
				if (exists(pos, this->mask_)) {
					return this->map_[pos].second;
				} else {
					throw std::out_of_range{"OpenHashMap::at"};
				}
			}

			template<typename KEY>
			V& at(const KEY& key) {
				const std::size_t pos{this->find(key)};
				if (exists(pos, this->mask_)) {
					return this->map_[pos].second;
				} else {
					throw std::out_of_range{"OpenHashMap::at"};
				}
			}

			template<typename KEY>
			const V& at(const KEY& key) const {
				const std::size_t pos{this->find(key)};
				if (exists(pos, this->mask_)) {
					return this->map_[pos].second;
				} else {
					throw std::out_of_range{"OpenHashMap::at"};
				}
			}


			bool contains(const K& key) const {
				return exists(this->find(key), this->mask_);
			}

			template<typename KEY>
			bool contains(const KEY& key) const {
				return exists(this->find(key), this->mask_);
			}


			std::size_t count(const K& key) const {
				return static_cast<std::size_t>(this->contains(key));
			}

			template<typename KEY>
			std::size_t count(const KEY& key) const {
				return static_cast<std::size_t>(this->contains(key));
			}


			// V& operator[](const K& key) {
			// 	if (this->map_ == nullptr) [[unlikely]] {
			// 		this->rehash(this->min_n_);
			// 	}
			//
			// 	const std::tuple<std::size_t, bool> position{this->find(key)};
			//
			// 	if (std::get<1>(position)) {
			// 		return this->map_[std::get<0>(position)].second;
			// 	} else {
			// 		return this->map_[this->insert(std::get<0>(position), key, V{})].second;
			// 	}
			// }
			//
			// V& operator[](const K&& key) {
			// 	if (this->map_ == nullptr) [[unlikely]] {
			// 		return this->map_[this->insert(0U, std::move(key), V{})].second;
			// 	}
			//
			// 	const std::tuple<std::size_t, bool> position{this->find(key)};
			// 	if (std::get<1>(position)) {
			// 		return this->map_[std::get<0>(position)].second;
			// 	} else {
			// 		return this->map_[this->insert(std::get<0>(position), std::move(key), V{})].second;
			// 	}
			// }
			//
			// template<typename KEY>
			// V& operator[](const KEY&& key) {
			// 	if (this->map_ == nullptr) [[unlikely]] {
			// 		this->rehash(this->min_n_);
			// 	}
			//
			// 	const std::tuple<std::size_t, bool> position{this->find(key)};
			//
			// 	if (std::get<1>(position)) {
			// 		return this->map_[std::get<0>(position)].second;
			// 	} else {
			// 		return this->map_[this->insert(std::get<0>(position), std::forward<KEY>(key), V{})].second;
			// 	}
			// }

		private:
			[[no_unique_address]] A<std::pair<K, V>> map_allocator_;
			[[no_unique_address]] A<std::uint_fast8_t> mask_allocator_;
			[[no_unique_address]] H hash_;
			[[no_unique_address]] E eq_;

			std::pair<K, V>* map_;
			std::uint_fast8_t* mask_;

			std::size_t size_;
			std::size_t n_;

			const float f_{};
			std::size_t max_size_;
			const std::size_t min_n_;

			explicit OpenHashMap(
				const std::tuple<std::size_t, float> size,
				const H& hash,
				const E& eq,
				const A<std::pair<K, V>>& map_allocator,
				const A<std::uint_fast8_t>& mask_allocator
			) : map_allocator_{std::allocator_traits<A<std::pair<K, V>>>::select_on_container_copy_construction(map_allocator)},
				mask_allocator_{std::allocator_traits<A<std::uint_fast8_t>>::select_on_container_copy_construction(mask_allocator)},
				hash_{hash},
				eq_{eq},
				map_{nullptr},
				mask_{nullptr},
				size_{0U},
				n_{std::get<0>(size)},
				f_{std::get<1>(size)},
				max_size_{maxSize(std::get<0>(size), std::get<1>(size))},
				min_n_{std::get<0>(size)} {
				const std::size_t n{std::get<0>(size)};
				this->map_ = std::allocator_traits<A<std::pair<K, V>>>::allocate(this->map_allocator_, n);

				const std::size_t mask_n{maskSize<std::uint_fast8_t>(n)};
				this->mask_ = std::allocator_traits<A<std::uint_fast8_t>>::allocate(this->mask_allocator_, mask_n);
				std::fill(this->mask_, this->mask_ + mask_n, 0U);
			}

			static bool exists(const std::size_t pos, const std::uint_fast8_t* const mask) {
				return static_cast<bool>((mask[maskPos<std::uint_fast8_t>(pos)] >> maskBit<std::uint_fast8_t>(pos)) & 1U);
			}

			static void set(const std::size_t pos, std::uint_fast8_t* const mask) {
				mask[maskPos<std::uint_fast8_t>(pos)] |= (1U << maskBit<std::uint_fast8_t>(pos));
			}

			static void unset(const std::size_t pos, std::uint_fast8_t* const mask) {
				mask[maskPos<std::uint_fast8_t>(pos)] &= ~(1U << maskBit<std::uint_fast8_t>(pos));
			}

			template<typename KEY>
			std::size_t find(const KEY& key) const {
				const std::size_t mask{this->n_ - 1U};

				std::size_t pos{};

				pos = (this->hash_(key) & mask);
				if (!exists(pos, this->mask_) || this->eq_(key, this->map_[pos].first)) {
					return pos;
				}

				while (true) {
					pos = ((pos + 1U) & mask);
					if (!exists(pos, this->mask_) || this->eq_(key, this->map_[pos].first)) {
						return pos;
					}
				}
			}

			// template<typename KEY, typename VALUE>
			// std::size_t insert(const std::size_t pos, KEY&& key, VALUE&& value) {
			// 	std::size_t new_pos{pos};
			// 	if (this->size_ == this->max_size_) {
			// 		this->rehash(arraySize(this->size_ + 1U, this->f_));
			// 		new_pos = std::get<0>(this->find(key));
			// 	}
			//
			// 	Entry& entry{this->map_[new_pos]};
			// 	entry.first = std::forward<KEY>(key);
			// 	entry.second = std::forward<VALUE>(value);
			// 	if (this->eq_(entry.first, this->null_)) {
			// 		this->null_pos_.emplace(new_pos);
			// 	}
			//
			// 	++this->size_;
			//
			// 	return new_pos;
			// }


			std::optional<std::size_t> rehash(const std::size_t new_n, const std::optional<std::size_t> i) {
				std::pair<K, V>* const new_map{std::allocator_traits<A<std::pair<K, V>>>::allocate(this->map_allocator_, new_n)};
				std::uint_fast8_t* const new_mask{std::allocator_traits<A<std::uint_fast8_t>>::allocate(this->mask_allocator_, maskSize<std::uint_fast8_t>(new_n))};

				std::optional<std::size_t> j{std::nullopt};

				const std::size_t mask{new_n - 1U};

				std::size_t pos{0U}, new_pos{0U};
				for (std::size_t cnt{0U}; cnt != this->size_; ++pos) {
					if (exists(pos, this->mask_)) {
						new_pos = (this->hash_(this->map_[pos].first) & mask);
						if (exists(new_pos, new_mask)) {
							new_pos = ((new_pos + 1U) & mask);
							while (exists(new_pos, new_mask)) {
							}
						}

						std::allocator_traits<A<std::pair<K, V>>>::construct(this->map_, &new_map[new_pos], std::move(this->map_[pos]));

						set(new_pos, new_mask);

						std::allocator_traits<A<std::pair<K, V>>>::destroy(this->map_allocator_, &this->map_[pos]);

						if (i.has_value() && i.value() == pos) {
							j = std::make_optional(new_pos);
						}

						++cnt;
					}
				}


				std::allocator_traits<A<std::pair<K, V>>>::deallocate(this->map_allocator_, this->map_, this->n_);
				std::allocator_traits<A<std::uint_fast8_t>>::deallocate(this->mask_allocator_, this->mask_, maskSize<std::uint_fast8_t>(this->n_));


				this->map_ = new_map;
				this->mask_ = new_mask;

				this->n_ = new_n;

				this->max_size_ = maxSize(this->n_, this->f_);


				return j;
			}
	};
}


#endif // OPEN_HASH_MAP
