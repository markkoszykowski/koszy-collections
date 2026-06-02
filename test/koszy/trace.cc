#include <algorithm>
#include <cstddef>
#include <cstdio>
#include <exception>
#include <memory>
#include <memory_resource>
#include <optional>
#include <stacktrace>
#include <typeinfo>
#include <utility>
#include <vector>

#include "test/koszy/trace.h"

namespace koszy::trace {
	// https://github.com/llvm/llvm-project/blob/main/libcxxabi/src/cxa_exception.cpp
	extern "C" void __real___cxa_throw(void* thrown_object, std::type_info* tinfo, void (*dest)(void*));

	alignas(alignof(std::max_align_t)) thread_local std::array<std::byte, BUFSIZ> _buffer{};
	thread_local std::pmr::monotonic_buffer_resource _resource{_buffer.data(), _buffer.size(), std::pmr::null_memory_resource()};
	thread_local std::pmr::polymorphic_allocator<std::stacktrace_entry> _allocator{std::addressof(_resource)};
	thread_local std::vector<std::pair<const void*, std::pmr::stacktrace>> _stacktraces{};

	inline auto predicate(const void* pointer) {
		return [pointer](const std::pair<const void*, auto>& pair) -> bool { return pair.first == pointer; };
	}

	extern "C" void __wrap___cxa_throw(void* thrown_object, std::type_info* tinfo, void (*dest)(void*)) {
		if (std::find_if(_stacktraces.cbegin(), _stacktraces.cend(), predicate(thrown_object)) == _stacktraces.cend()) {
			_stacktraces.emplace_back(thrown_object, std::pmr::stacktrace::current(1U, _allocator));
		}
		__real___cxa_throw(thrown_object, tinfo, dest);
	}

	std::optional<std::pmr::stacktrace> _trace(const void* object) noexcept {
		std::vector<std::pair<const void*, std::pmr::stacktrace>>::iterator it{std::remove_if(_stacktraces.begin(), _stacktraces.end(), predicate(object))};
		if (it == _stacktraces.end()) {
			return std::optional<std::pmr::stacktrace>{std::nullopt};
		}
		std::pair<const void*, std::pmr::stacktrace> stacktrace{std::move(*it)};
		_stacktraces.erase(it, _stacktraces.end());
		return std::optional<std::pmr::stacktrace>{std::move(stacktrace.second)};
	}
}
