package com.koszy.util;

import org.jspecify.annotations.NonNull;

public final class Enums {


	public static <T extends Enum<T>> T valueOf(final CharSequence string, final int offset, final int length, final T[] values) {
		Require.checkOffsetLength(offset, length, Strings.length(string));

		if (values == null) {
			return null;
		}

		for (final T value : values) {
			if (value != null && Strings.equals(string, offset, length, value.name())) {
				return value;
			}
		}

		return null;
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final int offset, final int length, final @NonNull Class<T> clazz) {
		return valueOf(string, offset, length, clazz.getEnumConstants());
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final T[] values) {
		return valueOf(string, 0, Strings.length(string), values);
	}

	public static <T extends Enum<T>> T valueOf(final CharSequence string, final @NonNull Class<T> clazz) {
		return valueOf(string, 0, Strings.length(string), clazz.getEnumConstants());
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final int offset, final int length, final T[] values) {
		Require.checkOffsetLength(offset, length, Strings.length(string));

		if (values == null) {
			return null;
		}

		for (final T value : values) {
			if (value != null && Strings.equalsIgnoreCase(string, offset, length, value.name())) {
				return value;
			}
		}

		return null;
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final int offset, final int length, final @NonNull Class<T> clazz) {
		return valueOfIgnoreCase(string, offset, length, clazz.getEnumConstants());
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final T[] values) {
		return valueOfIgnoreCase(string, 0, Strings.length(string), values);
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(final CharSequence string, final @NonNull Class<T> clazz) {
		return valueOfIgnoreCase(string, 0, Strings.length(string), clazz.getEnumConstants());
	}

	private Enums() {
		LangUtil.unreachable();
	}

}
