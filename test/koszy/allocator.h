#ifndef ALLOCATOR_H
#define ALLOCATOR_H

#include <atomic>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <source_location>
#include <unordered_map>


namespace koszy::collections {
	namespace {
		std::mutex globalMutex{};
		std::atomic<int> globalId{0};
		std::unordered_map<void*, int> globalBlocks{};
	}

	template<typename T>
	struct StatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::true_type;
		using propagate_on_container_move_assignment = std::true_type;
		using is_always_equal = std::false_type;

		std::allocator<T> allocator;
		int id;

		StatefulAllocator() : allocator{}, id{++globalId} {}

		StatefulAllocator(const StatefulAllocator&) : allocator{}, id{++globalId} {}

		StatefulAllocator(StatefulAllocator&& other) = default;


		StatefulAllocator& operator=(const StatefulAllocator&) {
			std::lock_guard<std::mutex> lock{globalMutex};

			for (const std::pair<void* const, int>& pair: globalBlocks) {
				if (pair.second == this->id) {
					throw std::logic_error{std::source_location::current().function_name()};
				}
			}

			this->id = ++globalId;

			return *this;
		}

		StatefulAllocator& operator=(StatefulAllocator&& other) = default;


		~StatefulAllocator() {
			std::lock_guard<std::mutex> lock{globalMutex};

			for (const std::pair<void* const, int>& pair: globalBlocks) {
				if (pair.second == this->id) {
					throw std::logic_error{std::source_location::current().function_name()};
				}
			}
		}


		T* allocate(const std::size_t n) {
			std::lock_guard<std::mutex> lock{globalMutex};

			T* const block{std::allocator_traits<std::allocator<T>>::allocate(this->allocator, n)};
			if (!globalBlocks.insert(std::make_pair(static_cast<void*>(block), this->id)).second) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			return block;
		}

		void deallocate(T* const pointer, const std::size_t n) {
			std::lock_guard<std::mutex> lock{globalMutex};

			if (globalBlocks.extract(static_cast<void*>(pointer)).mapped() != this->id) {
				throw std::logic_error{std::source_location::current().function_name()};
			}
			std::allocator_traits<std::allocator<T>>::deallocate(this->allocator, pointer, n);
		}
	};
}

#endif // ALLOCATOR_H
