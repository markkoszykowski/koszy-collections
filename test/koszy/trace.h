#ifndef TRACE_H
#define TRACE_H

#include <cstdlib>
#include <exception>
#include <iostream>
#include <memory>
#include <optional>
#include <stacktrace>

namespace koszy::trace {
	std::optional<std::stacktrace> _trace(const void*) noexcept;

	template<typename T>
	std::optional<std::stacktrace> trace(const T& t) noexcept {
		return _trace(std::addressof(t));
	}

	inline void terminate() noexcept {
		try {
			if (std::current_exception()) {
				std::rethrow_exception(std::current_exception());
			}
		} catch (const std::exception& exception) {
			std::cerr << exception.what() << '\n';
			if (const std::optional<std::stacktrace> stacktrace{trace<std::exception>(exception)}; stacktrace.has_value()) {
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
