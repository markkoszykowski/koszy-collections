package com.koszy.util;

import org.jspecify.annotations.NonNull;

public final class EnumUtil {


	private static <T extends Enum<T>> T valueOf0(final CharSequence string, final int begin, final int end, final T[] values) {
		if (values == null) {
			return null;
		}

		for (final T value : values) {
			if (value != null && StringUtil.equals(string, begin, end, value.name())) {
				return value;
			}
		}

		return null;
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final int begin, final int end, final T[] values) {
		StringUtil.check(string, begin, end);
		return valueOf0(string, begin, end, values);
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final int begin, final int end, final @NonNull Class<T> clazz) {
		return valueOf(string, begin, end, Require.nonNull(clazz).getEnumConstants());
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final T[] values) {
		return valueOf0(string, 0, StringUtil.length(string), values);
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final @NonNull Class<T> clazz) {
		return valueOf(string, 0, StringUtil.length(string), Require.nonNull(clazz).getEnumConstants());
	}


	private static <T extends Enum<T>> T valueOfIgnoreCase0(final CharSequence string, final int begin, final int end, final T[] values) {
		if (values == null) {
			return null;
		}

		for (final T value : values) {
			if (value != null && StringUtil.equalsIgnoreCase(string, begin, end, value.name())) {
				return value;
			}
		}

		return null;
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final int begin, final int end, final T[] values) {
		StringUtil.check(string, begin, end);
		return valueOfIgnoreCase0(string, begin, end, values);
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final int begin, final int end, final @NonNull Class<T> clazz) {
		return valueOfIgnoreCase(string, begin, end, Require.nonNull(clazz).getEnumConstants());
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final T[] values) {
		return valueOfIgnoreCase0(string, 0, StringUtil.length(string), values);
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final @NonNull Class<T> clazz) {
		return valueOfIgnoreCase(string, 0, StringUtil.length(string), Require.nonNull(clazz).getEnumConstants());
	}

	private EnumUtil() {
		LangUtil.unreachable();
	}

}
