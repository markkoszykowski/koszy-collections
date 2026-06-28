#ifndef TRACE_H
#define TRACE_H

#include <cstdlib>
#include <cstring>
#include <exception>
#include <iostream>
#include <memory>
#include <optional>
#include <stacktrace>

namespace koszy::trace {
	std::optional<std::pmr::stacktrace> _trace(const void*) noexcept;

	template<typename T>
	requires (!std::is_same_v<T, std::exception_ptr> || sizeof(T) != sizeof(void*))
	std::optional<std::pmr::stacktrace> trace(const T& t) noexcept {
		return _trace(std::addressof(t));
	}

	template<typename T>
	requires (std::is_same_v<T, std::exception_ptr> && sizeof(T) == sizeof(void*))
	std::optional<std::pmr::stacktrace> trace(const T exception) noexcept {
		void* pointer{nullptr};
		std::memcpy(std::addressof(pointer), std::addressof(exception), sizeof(pointer));
		return _trace(pointer);
	}

	inline void terminate() noexcept {
		try {
			if (std::current_exception()) {
				std::rethrow_exception(std::current_exception());
			}
		} catch (const std::exception& exception) {
			std::cerr << exception.what() << '\n';
			if (const std::optional<std::pmr::stacktrace> stacktrace{trace(exception)}; stacktrace.has_value()) {
				std::cerr << stacktrace.value() << '\n';
			}
		} catch (...) {
			std::cerr << "unknown exception\n";
			if (const std::optional<std::pmr::stacktrace> stacktrace{trace(std::current_exception())}; stacktrace.has_value()) {
				std::cerr << stacktrace.value() << '\n';
			}
		}
		std::exit(EXIT_FAILURE);
	}

	inline std::terminate_handler set_terminate() noexcept {
		return std::set_terminate(terminate);
	}
}

#endif // TRACE_H
