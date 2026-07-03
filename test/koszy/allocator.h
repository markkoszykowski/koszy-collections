#ifndef ALLOCATOR_H
#define ALLOCATOR_H

#include <cstdint>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <source_location>
#include <unordered_map>
#include <utility>

namespace koszy::collections {
	struct MemoryResource {
		std::mutex lock;
		std::unordered_map<void*, std::size_t> blocks;

		MemoryResource() = default;

		MemoryResource(const MemoryResource&) = delete;

		MemoryResource(MemoryResource&&) = delete;

		MemoryResource& operator=(const MemoryResource&) = delete;

		MemoryResource& operator=(MemoryResource&&) = delete;

		~MemoryResource() {
			if (!this->blocks.empty()) {
				throw std::logic_error{std::source_location::current().function_name()};
			}
		}
	};


	template <typename T>
	struct GlobalAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::false_type;
		using propagate_on_container_move_assignment = std::false_type;
		using propagate_on_container_swap = std::false_type;
		using is_always_equal = std::true_type;

		static_assert(std::allocator_traits<std::allocator<value_type>>::is_always_equal::value);

		[[no_unique_address]] std::allocator<value_type> allocator;

		[[nodiscard]] value_type* allocate(const std::size_t n) {
			return std::allocator_traits<std::allocator<value_type>>::allocate(this->allocator, n);
		}

		void deallocate(value_type* const pointer, const std::size_t n) {
			std::allocator_traits<std::allocator<value_type>>::deallocate(this->allocator, pointer, n);
		}
	};

	template <typename T>
	struct InternalAllocator {
		[[no_unique_address]] GlobalAllocator<T> allocator;
		std::shared_ptr<MemoryResource> resource;

		InternalAllocator() : allocator{}, resource{std::make_shared<MemoryResource>()} {}

		InternalAllocator(const InternalAllocator& other) : allocator{}, resource{other.resource} {}

		InternalAllocator(InternalAllocator&& other) noexcept : allocator{}, resource{other.resource} {};

		InternalAllocator& operator=(const InternalAllocator& other) {
			this->resource = other.resource;
			return *this;
		}

		InternalAllocator& operator=(InternalAllocator&& other) noexcept {
			this->resource = other.resource;
			return *this;
		}

		~InternalAllocator() noexcept = default;

		[[nodiscard]] bool operator==(const InternalAllocator& other) const {
			return this->resource == other.resource;
		};

		[[nodiscard]] T* allocate(const std::size_t n) {
			const std::lock_guard<std::mutex> lock{this->resource->lock};

			T* const pointer{std::allocator_traits<GlobalAllocator<T>>::allocate(this->allocator, n)};
			if (!this->resource->blocks.emplace(static_cast<void*>(pointer), n).second) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			return pointer;
		}

		void deallocate(T* const pointer, const std::size_t n) {
			const std::lock_guard<std::mutex> lock{this->resource->lock};

			const std::unordered_map<void*, std::size_t>::node_type value{this->resource->blocks.extract(static_cast<void*>(pointer))};
			if (value.empty() || value.mapped() != n) {
				throw std::logic_error{std::source_location::current().function_name()};
			}

			std::allocator_traits<GlobalAllocator<T>>::deallocate(this->allocator, pointer, n);
		}
	};

	template <typename T>
	struct StatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::false_type;
		using propagate_on_container_move_assignment = std::false_type;
		using propagate_on_container_swap = std::false_type;
		using is_always_equal = std::false_type;

		InternalAllocator<value_type> allocator;

		[[nodiscard]] bool operator==(const StatefulAllocator&) const = default;

		[[nodiscard]] value_type* allocate(const std::size_t n) {
			return this->allocator.allocate(n);
		}

		void deallocate(value_type* const pointer, const std::size_t n) {
			this->allocator.deallocate(pointer, n);
		}
	};

	template <typename T>
	struct PropagatingStatefulAllocator {
		using value_type = T;
		using propagate_on_container_copy_assignment = std::true_type;
		using propagate_on_container_move_assignment = std::true_type;
		using propagate_on_container_swap = std::true_type;
		using is_always_equal = std::false_type;

		InternalAllocator<value_type> allocator;

		[[nodiscard]] bool operator==(const PropagatingStatefulAllocator&) const = default;

		[[nodiscard]] value_type* allocate(const std::size_t n) {
			return this->allocator.allocate(n);
		}

		void deallocate(value_type* const pointer, const std::size_t n) {
			this->allocator.deallocate(pointer, n);
		}
	};
}

#endif // ALLOCATOR_H
