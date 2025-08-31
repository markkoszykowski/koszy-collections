#ifndef OPEN_HASH_MAP
#define OPEN_HASH_MAP

#include <cassert>
#include <functional>
#include <memory>
#include <optional>
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
		using Entry = std::pair<K, V>;

		public:
			OpenHashMap() : OpenHashMap{
				std::make_tuple(koszy::collections::hash::DEFAULT_INITIAL_CAPACITY, koszy::collections::hash::DEFAULT_LOAD_FACTOR),
				H{},
				E{},
				A<Entry>{}
			} {
			}

			explicit OpenHashMap(const std::size_t expected) : OpenHashMap{
				std::make_tuple(koszy::collections::hash::arraySize(expected, koszy::collections::hash::DEFAULT_LOAD_FACTOR), koszy::collections::hash::DEFAULT_LOAD_FACTOR),
				H{},
				E{},
				A<Entry>{}
			} {
			}

			explicit OpenHashMap(
				const std::size_t expected,
				const float f
			) : OpenHashMap{
				std::make_tuple(koszy::collections::hash::arraySize(expected, f), f),
				H{},
				E{},
				A<Entry>{}
			} {
			}

			explicit OpenHashMap(const H& hash, const E& eq) : OpenHashMap{
				std::make_tuple(koszy::collections::hash::DEFAULT_INITIAL_CAPACITY, koszy::collections::hash::DEFAULT_LOAD_FACTOR),
				hash,
				eq,
				A<Entry>{}
			} {
			}

			explicit OpenHashMap(const A<Entry>& allocator) : OpenHashMap{
				std::make_tuple(koszy::collections::hash::DEFAULT_INITIAL_CAPACITY, koszy::collections::hash::DEFAULT_LOAD_FACTOR),
				H{},
				E{},
				allocator
			} {
			}

			explicit OpenHashMap(
				const std::size_t expected,
				const float f,
				const H& hash,
				const E& eq,
				const A<Entry>& allocator
			) : OpenHashMap{
				std::make_tuple(koszy::collections::hash::arraySize(expected, f), f),
				hash,
				eq,
				allocator
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
				if (this->map_ != nullptr) {
					for (std::size_t i{0U}; i != this->n_; ++i) {
						std::allocator_traits<A<Entry>>::destroy(this->allocator_, &this->map_[i]);
					}
					this->allocator_.deallocate(this->map_, this->n_);
				}
			}

			std::size_t size() const {
				return this->size_;
			}

			bool empty() const {
				return this->size_ == 0U;
			}

			std::size_t max_size() const {
				return std::allocator_traits<A<Entry>>::max_size(this->allocator_);
			}

			void clear() {
				if (this->size_ == 0U) {
					return;
				}

				std::size_t pos{this->n_};
				Entry* entry{};

				for (std::size_t i{this->size_}; i != 0U; --i) {
					while (
						this->eq_((entry = &this->map_[--pos])->first, this->null_) &&
						(!this->null_pos_.has_value() || pos != this->null_pos_.value())
					);
					entry->first = K{};
					entry->second = V{};
				}
				this->size_ = 0U;
				this->null_pos_.reset();
			}

			V& at(const K& key) const {
				if (this->map_ == nullptr) [[unlikely]] {
					throw std::out_of_range{"OpenHashMap::at"};
				}

				const std::tuple<std::size_t, bool> position{this->find(key)};
				if (std::get<1>(position)) {
					return this->map_[std::get<0>(position)].second;
				} else {
					throw std::out_of_range{"OpenHashMap::at"};
				}
			}

			bool contains(const K& key) const {
				return this->map_ != nullptr && std::get<1>(this->find(key));
			}

			template<typename KEY>
			bool contains(const KEY& key) const {
				return this->map_ != nullptr && std::get<1>(this->find(key));
			}


			std::size_t count(const K& key) const {
				return static_cast<std::size_t>(this->contains(key));
			}

			template<typename KEY>
			std::size_t count(const KEY& key) const {
				return static_cast<std::size_t>(this->contains(key));
			}


			V& operator[](const K& key) {
				if (this->map_ == nullptr) [[unlikely]] {
					this->rehash(this->min_n_);
				}

				const std::tuple<std::size_t, bool> position{this->find(key)};

				if (std::get<1>(position)) {
					return this->map_[std::get<0>(position)].second;
				} else {
					return this->map_[this->insert(std::get<0>(position), key, V{})].second;
				}
			}

			V& operator[](const K&& key) {
				if (this->map_ == nullptr) [[unlikely]] {
					return this->map_[this->insert(0U, std::move(key), V{})].second;
				}

				const std::tuple<std::size_t, bool> position{this->find(key)};
				if (std::get<1>(position)) {
					return this->map_[std::get<0>(position)].second;
				} else {
					return this->map_[this->insert(std::get<0>(position), std::move(key), V{})].second;
				}
			}

			template<typename KEY>
			V& operator[](const KEY&& key) {
				if (this->map_ == nullptr) [[unlikely]] {
					this->rehash(this->min_n_);
				}

				const std::tuple<std::size_t, bool> position{this->find(key)};

				if (std::get<1>(position)) {
					return this->map_[std::get<0>(position)].second;
				} else {
					return this->map_[this->insert(std::get<0>(position), std::forward<KEY>(key), V{})].second;
				}
			}

		private:
			[[no_unique_address]] A<Entry> allocator_;
			[[no_unique_address]] H hash_;
			[[no_unique_address]] E eq_;

			Entry* map_;

			std::size_t mask_;

			std::size_t size_;
			std::size_t n_;

			const float f_{};
			std::size_t max_size_;
			const std::size_t min_n_;

			const K null_;
			std::optional<std::size_t> null_pos_;

			explicit OpenHashMap(
				const std::tuple<std::size_t, float> size,
				const H& hash,
				const E& eq,
				const A<Entry>& allocator
			) : allocator_{std::allocator_traits<A<Entry>>::select_on_container_copy_construction(allocator)},
				hash_{hash},
				eq_{eq},
				map_{nullptr},
				mask_{koszy::collections::hash::arrayMask(std::get<0>(size))},
				size_{0U},
				n_{std::get<0>(size)},
				f_{std::get<1>(size)},
				max_size_{koszy::collections::hash::maxSize(std::get<0>(size), std::get<1>(size))},
				min_n_{std::max(std::get<0>(size), koszy::collections::hash::DEFAULT_MIN_CAPACITY)},
				null_{K{}},
				null_pos_{std::nullopt} {
				if (0U < std::get<0>(size)) {
					this->map_ = std::allocator_traits<A<Entry>>::allocate(this->allocator_, std::get<0>(size));
					for (std::size_t i{0U}; i != std::get<0>(size); ++i) {
						std::allocator_traits<A<Entry>>::construct(this->allocator_, &this->map_[i]);
					}
				}
			}

			template<typename KEY>
			std::tuple<std::size_t, bool> find(KEY&& key) const {
				assert(this->map_ != nullptr);

				std::size_t pos{};
				const K* k{};

				if (
					this->eq_(*(k = &this->map_[pos = (this->hash_(key) & this->mask_)].first), this->null_) &&
					(!this->null_pos_.has_value() || pos != this->null_pos_.value())
				) {
					return std::make_tuple(pos, false);
				}
				if (this->eq_(key, *k)) {
					return std::make_tuple(pos, true);
				}

				while (true) {
					if (
						this->eq_(*(k = &this->map_[++pos & this->mask_].first), this->null_) &&
						(!this->null_pos_.has_value() || pos != this->null_pos_.value())
					) {
						return std::make_tuple(pos, false);
					}
					if (this->eq_(key, *k)) {
						return std::make_tuple(pos, true);
					}
				}
			}

			template<typename KEY, typename VALUE>
			std::size_t insert(const std::size_t pos, KEY&& key, VALUE&& value) {
				std::size_t new_pos{pos};
				if (this->size_ == this->max_size_) {
					this->rehash(koszy::collections::hash::arraySize(this->size_ + 1U, this->f_));
					new_pos = std::get<0>(this->find(key));
				}

				Entry& entry{this->map_[new_pos]};
				entry.first = std::forward<KEY>(key);
				entry.second = std::forward<VALUE>(value);
				if (this->eq_(entry.first, this->null_)) {
					this->null_pos_.emplace(new_pos);
				}

				++this->size_;

				return new_pos;
			}

			void rehash(const std::size_t new_n) {
				Entry* const new_map{std::allocator_traits<A<Entry>>::allocate(this->allocator_, new_n)};
				for (std::size_t i{0U}; i != new_n; ++i) {
					std::allocator_traits<A<Entry>>::construct(this->allocator_, &new_map[i]);
				}
				const std::size_t new_mask{koszy::collections::hash::arrayMask(new_n)};
				const std::size_t new_max_size{koszy::collections::hash::maxSize(new_n, this->f_)};
				std::optional<std::size_t> new_null_pos{std::nullopt};

				if (this->map_ != nullptr) [[likely]] {
					std::size_t pos{this->n_}, new_pos{};
					Entry* entry{},* new_entry{};

					for (std::size_t i{this->size_}; i != 0U; --i) {
						while (
							this->eq_((entry = &this->map_[--pos])->first, this->null_) &&
							(!this->null_pos_.has_value() || pos != this->null_pos_.value())
						);
						if (
							this->eq_((new_entry = &new_map[new_pos = (this->hash_(this->map_[pos].first) & new_mask)])->first, this->null_) &&
							(!new_null_pos.has_value() || new_pos != new_null_pos.value())
						) {
							while (
								this->eq_((new_entry = &new_map[++new_pos & new_mask])->first, this->null_) &&
								(!new_null_pos.has_value() || new_pos != new_null_pos.value())
							);
						}

						*new_entry = std::move(this->map_[pos]);
						if (this->eq_(new_entry->first, this->null_)) {
							new_null_pos.emplace(new_pos);
						}

						std::allocator_traits<A<Entry>>::destroy(this->allocator_, entry);
					}

					std::allocator_traits<A<Entry>>::deallocate(this->allocator_, this->map_, this->n_);
				}

				this->map_ = new_map;
				this->mask_ = new_mask;
				this->n_ = new_n;
				this->max_size_ = new_max_size;
				this->null_pos_ = new_null_pos;
			}
	};
}


#endif // OPEN_HASH_MAP
