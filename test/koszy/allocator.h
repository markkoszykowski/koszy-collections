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
	struct Resources {
		std::mutex lock;
		std::atomic<int> id;
		std::unordered_map<void*, int> blocks;
	};

	template<typename T>
	struct StatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::true_type;
		using propagate_on_container_move_assignment = std::true_type;
		using propagate_on_container_swap = std::true_type;
		using is_always_equal = std::false_type;


		[[no_unique_address]] std::allocator<T> allocator;
		std::shared_ptr<Resources> resources;
		std::optional<int> id;


		static std::optional<int> copy(const StatefulAllocator& allocator) {
			return std::make_optional(++allocator.resources->id);
		}

		static std::optional<int> move(StatefulAllocator&& allocator) {
			return std::exchange(allocator.id, std::nullopt);
		}

		static void empty(const StatefulAllocator& allocator) {
			if (allocator.id.has_value()) {
				std::lock_guard<std::mutex> lock{allocator.resources->lock};

				for (const std::pair<void* const, int>& pair: allocator.resources->blocks) {
					if (pair.second == allocator.id.value()) {
						throw std::logic_error{std::source_location::current().function_name()};
					}
				}
			}
		}


		StatefulAllocator() : allocator{}, resources{std::make_shared<Resources>()}, id{std::make_optional(++this->resources->id)} {}

		StatefulAllocator(const StatefulAllocator& other) : allocator{}, resources{other.resources}, id{copy(other)} {}

		StatefulAllocator(StatefulAllocator&& other) noexcept : allocator{}, resources{std::move(other.resources)}, id{move(std::move(other))} {};

		StatefulAllocator& operator=(const StatefulAllocator& other) {
			empty(*this);
			this->resources = other.resources;
			this->id = copy(other);
			return *this;
		}

		StatefulAllocator& operator=(StatefulAllocator&& other) noexcept {
			empty(*this);
			this->resources = std::move(other.resources);
			this->id = move(std::move(other));
			return *this;
		}

		~StatefulAllocator() {
			empty(*this);
		}


		T* allocate(const std::size_t n) {
			std::lock_guard<std::mutex> lock{this->resources->lock};

			T* const block{std::allocator_traits<std::allocator<T>>::allocate(this->allocator, n)};
			if (!this->resources->blocks.insert(std::make_pair(static_cast<void*>(block), this->id.value())).second) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			return block;
		}

		void deallocate(T* const pointer, const std::size_t n) {
			std::lock_guard<std::mutex> lock{this->resources->lock};

			if (this->resources->blocks.extract(static_cast<void*>(pointer)).mapped() != this->id.value()) {
				throw std::logic_error{std::source_location::current().function_name()};
			}
			std::allocator_traits<std::allocator<T>>::deallocate(this->allocator, pointer, n);
		}
	};
}

#endif // ALLOCATOR_H
