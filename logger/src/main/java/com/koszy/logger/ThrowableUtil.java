package com.koszy.logger;

import com.koszy.util.LangUtil;

final class ThrowableUtil {

	/// @see StackTraceElement#NATIVE_METHOD
	static final String NATIVE_METHOD = "Native Method";
	/// @see StackTraceElement#UNKNOWN_SOURCE
	static final String UNKNOWN_SOURCE = "Unknown Source";

	/// @see StackTraceElement#toString()
	static StringBuilder append(final StackTraceElement element, final StringBuilder builder) {
		builder.append(element.getClassName()).append('.').append(element.getMethodName());
		final String fileName;
		builder.append('(');
		if (element.isNativeMethod()) {
			builder.append(NATIVE_METHOD);
		} else if ((fileName = element.getFileName()) == null) {
			builder.append(UNKNOWN_SOURCE);
		} else {
			builder.append(fileName);
			final int lineNumber = element.getLineNumber();
			if (0 <= lineNumber) {
				builder.append(':').append(lineNumber);
			}
		}
		builder.append(')');
		return builder;
	}

	/// @see Throwable#toString()
	static StringBuilder append(final Throwable throwable, final StringBuilder builder) {
		builder.append(throwable.getClass().getName());
		final String localizedMessage = throwable.getLocalizedMessage();
		if (localizedMessage != null) {
			builder.append(": ").append(localizedMessage);
		}
		return builder;
	}

	private ThrowableUtil() {
		LangUtil.unreachable();
	}

}
