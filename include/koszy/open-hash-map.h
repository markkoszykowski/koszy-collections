#ifndef OPEN_HASH_MAP
#define OPEN_HASH_MAP

#include <memory>

#include "koszy/hash-common.h"

namespace koszy::collections::hash {
	template<
		typename K,
		typename V,
		typename H = std::hash<K>,
		typename E = std::equal_to<K>,
		template<typename T> typename A=std::allocator,
		bool PREALLOCATE = false
	>
	class OpenHashMap {
		struct Entry {
			K key;
			V value;
		};

		public:
			OpenHashMap() : OpenHashMap{DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR} {
			}

			OpenHashMap(const std::size_t expected) : OpenHashMap{expected, DEFAULT_LOAD_FACTOR} {
			}

			OpenHashMap(const std::size_t expected, const float f) : OpenHashMap{expected, arraySize(expected, f), f} {
			}

			~OpenHashMap() {
				if (this->map_ != nullptr) {
					for (std::size_t i{0U}; i != this->n_; ++i) {
						std::allocator_traits<A<Entry>>::destroy(this->allocator_, &this->map_[i]);
					}
					if (this->contains_null_) {
						std::allocator_traits<A<Entry>>::destroy(this->allocator_, &this->map_[this->n_]);
					}
					this->allocator_.deallocate(this->map_, this->n_ + 1U);
				}
			}

			std::size_t size() const {
				return this->size_;
			}

			void insert(const K& key, const V& value) {
				// TODO
			}

		private:
			A<Entry> allocator_;

			Entry* map_;

			std::size_t mask_;

			std::size_t n_;
			std::size_t max_fill_;
			const std::size_t min_n_;

			std::size_t size_;
			float f_;

			const K null_;
			bool contains_null_;

			OpenHashMap(const std::size_t expected, const std::size_t n, const float f) : allocator_{A<Entry>{}},
				map_{nullptr},
				mask_{n - 1},
				n_{n},
				max_fill_{maxFill(n, f)},
				min_n_{n},
				size_{0U},
				null_{K{}},
				contains_null_{false} {
				if (PREALLOCATE) {
					this->map_ = this->allocator_.allocate(n + 1U);
					for (std::size_t i{0U}; i != n; ++i) {
						std::allocator_traits<A<Entry>>::construct(this->allocator_, &this->map_[i]);
					}
				}
			}

			std::size_t real_size() const {
				return this->size_ - static_cast<std::size_t>(this->contains_null_);
			}

			void rehash(const std::size_t new_n) {
				const std::size_t mask{new_n - 1U};
				Entry* new_entry{this->allocator_.allocate(new_n + 1U)};
				for (std::size_t i{0U}; i != new_n; ++i) {
					std::allocator_traits<A<Entry>>::construct(this->allocator_, &new_entry[i]);
				}

				std::size_t pos{this->n_}, new_pos{};
				for (std::size_t i{this->real_size()}; i != 0U; --i) {
					while (E(this->map_[--pos], this->null_));
					if (E(new_entry[new_pos = (H(this->map_[pos]) & mask)], this->null_)) {
						while (E(new_entry[new_pos = ((new_pos + 1) & mask)], this->null_));
					}
					new_entry[new_pos] = std::move(this->map_[pos]);

					std::allocator_traits<A<Entry>>::destroy(this->allocator_, &this->map_[pos]);
				}
				if (this->contains_null_) {
					std::allocator_traits<A<Entry>>::construct(this->allocator_, &new_entry[new_n], std::move(this->map_[this->n_]));

					std::allocator_traits<A<Entry>>::destroy(this->allocator_, &this->map_[this->n_]);
				}

				this->allocator_.deallocate(this->map_, this->n_ + 1U);

				this->map_ = new_entry;
				this->n_ = new_n;
				this->mask_ = mask;
				this->max_fill_ = maxFill(this->n_, this->f_);
			}
	};
}


#endif // OPEN_HASH_MAP
