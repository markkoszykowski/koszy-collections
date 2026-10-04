package com.koszy.util;

import org.jspecify.annotations.NonNull;

public final class LangUtil {

	public static <T> @NonNull T rethrowUnchecked(final @NonNull Throwable throwable) {
		return LangUtil.<RuntimeException, T>rethrow(throwable);
	}

	@SuppressWarnings(value = {"unchecked"})
	private static <T extends Throwable, R> @NonNull R rethrow(final @NonNull Throwable throwable) throws T {
		throw (T) throwable;
	}

	private static final class Unreachable extends Error {
	}

	public static <T> @NonNull T unreachable() {
		throw new Unreachable();
	}

	private LangUtil() {
		unreachable();
	}

}
