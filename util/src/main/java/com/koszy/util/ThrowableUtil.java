package com.koszy.util;

import java.util.Objects;

public final class ThrowableUtil {

	public static <T extends Throwable> T suppress(final T throwable, final T suppressed) {
		if (throwable == null) {
			return suppressed;
		}
		if (suppressed != null) {
			throwable.addSuppressed(suppressed);
		}
		return throwable;
	}

	public static boolean equals(final StackTraceElement left, final StackTraceElement right) {
		if (left == right) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.getLineNumber() == right.getLineNumber() &&
				Objects.equals(left.getMethodName(), right.getMethodName()) &&
				Objects.equals(left.getClassName(), right.getClassName()) &&
				Objects.equals(left.getFileName(), right.getFileName());
	}

	public static boolean equals(final StackTraceElement[] left, final StackTraceElement[] right) {
		return ArrayUtil.equals(ThrowableUtil::equals, left, right);
	}

	public static boolean equals(final Throwable left, final Throwable right) {
		Throwable l = left;
		Throwable r = right;
		for (; l != null && r != null && l != r; l = l.getCause(), r = r.getCause()) {
			if (!Objects.equals(l.getClass(), r.getClass()) || !Objects.equals(l.getMessage(), r.getMessage()) || !equals(l.getStackTrace(), r.getStackTrace())) {
				return false;
			}
		}
		return l == r;
	}

	public static int hashCode(final Throwable throwable) {
		if (throwable == null) {
			return 0;
		}

		int hash = 1;
		hash = 31 * hash + Objects.hashCode(throwable.getClass());
		hash = 31 * hash + Objects.hashCode(throwable.getMessage());
		return hash;
	}

	private ThrowableUtil() {
		LangUtil.unreachable();
	}

}
