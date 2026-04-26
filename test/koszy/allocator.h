#ifndef ALLOCATOR_H
#define ALLOCATOR_H

#include <atomic>
#include <cstdint>
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
		std::unordered_map<void*, std::pair<int, std::size_t>> blocks;
	};

	template<typename T>
	struct InternalAllocator {
		[[no_unique_address]] std::allocator<T> allocator;
		std::shared_ptr<Resources> resources;
		std::optional<int> id;


		static std::optional<int> copy(const InternalAllocator& allocator) {
			return std::make_optional(++allocator.resources->id);
		}

		static std::optional<int> move(InternalAllocator&& allocator) {
			return std::exchange(allocator.id, std::nullopt);
		}

		static void empty(const InternalAllocator& allocator) {
			if (allocator.id.has_value()) {
				std::lock_guard<std::mutex> lock{allocator.resources->lock};

				for (const std::pair<void* const, std::pair<int, std::size_t>>& pair: allocator.resources->blocks) {
					if (pair.second.first == allocator.id.value()) {
						throw std::logic_error{std::source_location::current().function_name()};
					}
				}
			}
		}


		InternalAllocator() : allocator{}, resources{std::make_shared<Resources>()}, id{std::make_optional(++this->resources->id)} {}

		InternalAllocator(const InternalAllocator& other) : allocator{}, resources{other.resources}, id{copy(other)} {}

		InternalAllocator(InternalAllocator&& other) noexcept : allocator{}, resources{std::move(other.resources)}, id{move(std::move(other))} {};

		InternalAllocator& operator=(const InternalAllocator& other) {
			empty(*this);
			this->resources = other.resources;
			this->id = copy(other);
			return *this;
		}

		InternalAllocator& operator=(InternalAllocator&& other) noexcept {
			empty(*this);
			this->resources = std::move(other.resources);
			this->id = move(std::move(other));
			return *this;
		}

		~InternalAllocator() {
			empty(*this);
		}


		T* allocate(const std::size_t n) {
			std::lock_guard<std::mutex> lock{this->resources->lock};

			T* const block{std::allocator_traits<std::allocator<T>>::allocate(this->allocator, n)};
			if (!this->resources->blocks.insert(std::make_pair(static_cast<void*>(block), std::make_pair(this->id.value(), n))).second) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			return block;
		}

		void deallocate(T* const pointer, const std::size_t n) {
			std::lock_guard<std::mutex> lock{this->resources->lock};

			const std::pair<int, std::size_t> value{this->resources->blocks.extract(static_cast<void*>(pointer)).mapped()};
			if (value.first != this->id.value() || value.second != n) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			std::allocator_traits<std::allocator<T>>::deallocate(this->allocator, pointer, n);
		}
	};

	template<typename T>
	struct StatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::false_type;
		using propagate_on_container_move_assignment = std::false_type;
		using propagate_on_container_swap = std::false_type;
		using is_always_equal = std::false_type;

		InternalAllocator<T> allocator;

		T* allocate(const std::size_t n) {
			return this->allocator.allocate(n);
		}

		void deallocate(T* const pointer, const std::size_t n) {
			this->allocator.deallocate(pointer, n);
		}
	};

	template<typename T>
	struct PropagatingStatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::true_type;
		using propagate_on_container_move_assignment = std::true_type;
		using propagate_on_container_swap = std::true_type;
		using is_always_equal = std::false_type;

		InternalAllocator<T> allocator;

		T* allocate(const std::size_t n) {
			return this->allocator.allocate(n);
		}

		void deallocate(T* const pointer, const std::size_t n) {
			this->allocator.deallocate(pointer, n);
		}
	};
}

#endif // ALLOCATOR_H
