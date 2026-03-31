#ifndef ALLOCATOR_H
#define ALLOCATOR_H

#include <atomic>
#include <memory>
#include <mutex>
#include <optional>
#include <stdexcept>
#include <source_location>
#include <unordered_map>
#include <utility>


namespace koszy::collections {
	namespace {
		std::mutex GLOBAL_MUTEX{};
		std::atomic<int> GLOBAL_ID{0};
		std::unordered_map<void*, int> GLOBAL_BLOCKS{};
	}

	template<typename T>
	struct StatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::true_type;
		using propagate_on_container_move_assignment = std::true_type;
		using is_always_equal = std::false_type;


		std::allocator<T> allocator;
		std::optional<int> id;


		static std::optional<int> copy(const StatefulAllocator& allocator) {
			if (!allocator.id.has_value()) {
				throw std::logic_error{std::source_location::current().function_name()};
			}
			return std::make_optional(++GLOBAL_ID);
		}

		static std::optional<int> move(StatefulAllocator&& allocator) {
			if (!allocator.id.has_value()) {
				throw std::logic_error{std::source_location::current().function_name()};
			}
			return std::exchange(allocator.id, std::nullopt);
		}

		static void empty(const StatefulAllocator& allocator) {
			if (!allocator.id.has_value()) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			std::lock_guard<std::mutex> lock{GLOBAL_MUTEX};

			for (const std::pair<void* const, int>& pair: GLOBAL_BLOCKS) {
				if (pair.second == allocator.id.value()) {
					throw std::logic_error{std::source_location::current().function_name()};
				}
			}
		}


		StatefulAllocator() : allocator{}, id{std::make_optional(++GLOBAL_ID)} {}

		StatefulAllocator(const StatefulAllocator& other) : allocator{}, id{copy(other)} {}

		StatefulAllocator(StatefulAllocator&& other) : allocator{}, id{move(std::move(other))} {};

		StatefulAllocator& operator=(const StatefulAllocator& other) {
			empty(*this);
			this->id = copy(other);
			return *this;
		}

		StatefulAllocator& operator=(StatefulAllocator&& other) {
			empty(*this);
			this->id = move(std::move(other));
			return *this;
		}

		~StatefulAllocator() {
			if (this->id.has_value()) {
				empty(*this);
			}
		}


		T* allocate(const std::size_t n) {
			std::lock_guard<std::mutex> lock{GLOBAL_MUTEX};

			T* const block{std::allocator_traits<std::allocator<T>>::allocate(this->allocator, n)};
			if (!GLOBAL_BLOCKS.insert(std::make_pair(static_cast<void*>(block), this->id.value())).second) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			return block;
		}

		void deallocate(T* const pointer, const std::size_t n) {
			std::lock_guard<std::mutex> lock{GLOBAL_MUTEX};

			if (GLOBAL_BLOCKS.extract(static_cast<void*>(pointer)).mapped() != this->id.value()) {
				throw std::logic_error{std::source_location::current().function_name()};
			}
			std::allocator_traits<std::allocator<T>>::deallocate(this->allocator, pointer, n);
		}
	};
}

#endif // ALLOCATOR_H
