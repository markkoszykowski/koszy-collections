#include <algorithm>
#include <exception>
#include <optional>
#include <stacktrace>
#include <typeinfo>
#include <utility>
#include <vector>

#include "test/koszy/trace.h"

namespace koszy::trace {
	// https://github.com/llvm/llvm-project/blob/main/libcxxabi/src/cxa_exception.cpp
	extern "C" void __real___cxa_throw(void* thrown_object, std::type_info* tinfo, void (*dest)(void*));

	thread_local std::vector<std::pair<const void*, std::stacktrace>> stacktraces{};

	inline auto predicate(const void* pointer) {
		return [pointer](const std::pair<const void*, auto>& pair) -> bool { return pair.first == pointer; };
	}

	extern "C" void __wrap___cxa_throw(void* thrown_object, std::type_info* tinfo, void (*dest)(void*)) {
		if (std::find_if(stacktraces.cbegin(), stacktraces.cend(), predicate(thrown_object)) == stacktraces.cend()) {
			stacktraces.emplace_back(thrown_object, std::stacktrace::current(1));
		}
		__real___cxa_throw(thrown_object, tinfo, dest);
	}

	std::optional<std::stacktrace> _trace(const void* object) {
		std::vector<std::pair<const void*, std::stacktrace>>::iterator it{std::remove_if(stacktraces.begin(), stacktraces.end(), predicate(object))};
		if (it == stacktraces.end()) {
			return std::optional<std::stacktrace>{std::nullopt};
		}
		std::pair<const void*, std::stacktrace> stacktrace{std::move(*it)};
		stacktraces.erase(it, stacktraces.end());
		return std::optional<std::stacktrace>{std::move(stacktrace.second)};
	}
}
