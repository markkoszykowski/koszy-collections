#ifndef OPEN_HASH_MAP
#define OPEN_HASH_MAP

#include <utility>

#include "koszy/array-common.h"
#include "koszy/array-mask.h"

namespace koszy::collections::hash {
	template<
		typename K,
		typename V,
		typename H = std::hash<K>,
		typename E = std::equal_to<K>,
		typename A = std::allocator<std::pair<K, V>>,
		typename M = std::uintptr_t,
		typename MA = std::allocator<M>>
	class OpenHashMap {
		using Mask = mask::ArrayMask<M, MA>;
		using Entry = std::pair<K, V>;
		using Deleter = HashDeleter<Entry, A, M, MA>;

		public:
			OpenHashMap() : allocator_{}, hash_{}, equal_{}, mask_{}, map_{nullptr, Deleter{this->allocator_, this->mask_, 0U}}, size_{0U} {}

			[[nodiscard]] constexpr bool empty() const {
				return this->size_ == 0U;
			}

			[[nodiscard]] constexpr std::size_t size() const {
				return this->size_;
			}

		private:
			[[no_unique_address]] A allocator_;
			[[no_unique_address]] H hash_;
			[[no_unique_address]] E equal_;
			Mask mask_;

			std::unique_ptr<Entry[], Deleter> map_;
			std::size_t size_;

			[[nodiscard]] constexpr std::size_t capacity() const {
				return this->map_.get_deleter().size_;
			}
	};
}


#endif // OPEN_HASH_MAP
