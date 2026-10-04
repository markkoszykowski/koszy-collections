package com.koszy.util;

import java.io.IOException;

public final class Strings {

	public static final String EMPTY = "";

	public static boolean isEmpty(final String string) {
		return string == null || string.isEmpty();
	}

	public static String empty() {
		return EMPTY;
	}

	public static String toNullableString(final boolean b) {
		return String.valueOf(b);
	}

	public static String toNullableString(final byte b) {
		return String.valueOf(b);
	}

	public static String toNullableString(final short s) {
		return String.valueOf(s);
	}

	public static String toNullableString(final int i) {
		return String.valueOf(i);
	}

	public static String toNullableString(final long l) {
		return String.valueOf(l);
	}

	public static String toNullableString(final char c) {
		return String.valueOf(c);
	}

	public static String toNullableString(final float f) {
		return String.valueOf(f);
	}

	public static String toNullableString(final double d) {
		return String.valueOf(d);
	}

	public static String toNullableString(final Object object) {
		return object == null ? null : object.toString();
	}

	public static String toNullableString(final CharSequence string) {
		return string == null ? null : string.length() == 0 ? EMPTY : string.toString();
	}

	public static int length(final CharSequence string) {
		return string == null ? 0 : string.length();
	}

	public static boolean isAscii(final byte b) {
		return 0 <= b;
	}

	public static boolean isAscii(final char c) {
		return c < 128;
	}

	public static boolean isAscii(final CharSequence string, final int offset, final int length) {
		Require.checkOffsetLength(offset, length, length(string));

		for (int i = 0; i < length; ++i) {
			if (!isAscii(string.charAt(offset + i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAscii(final CharSequence string) {
		return isAscii(string, 0, length(string));
	}

	public static byte toAscii(final char c) {
		Require.isTrue(isAscii(c));
		return (byte) c;
	}

	public static byte toAscii(final char c, final byte defaultt) {
		return isAscii(c) ? (byte) c : defaultt;
	}

	public static <T extends Appendable> T append(final T appendable, final Object object) {
		if (appendable != null && object != null) {
			try {
				appendable.append(object.toString());
			} catch (final IOException exception) {
				LangUtil.rethrowUnchecked(exception);
			}
		}
		return appendable;
	}

	public static <T extends Appendable> T append(final T appendable, final CharSequence string, final int offset, final int length) {
		if (appendable != null && string != null) {
			try {
				appendable.append(string, offset, offset + length);
			} catch (final IOException exception) {
				LangUtil.rethrowUnchecked(exception);
			}
		}
		return appendable;
	}

	public static <T extends Appendable> T append(final T appendable, final CharSequence string) {
		if (appendable != null && string != null) {
			try {
				appendable.append(string);
			} catch (final IOException exception) {
				LangUtil.rethrowUnchecked(exception);
			}
		}
		return appendable;
	}

	public static String capitalize(final String string) {
		if (isEmpty(string)) {
			return string;
		}
		final char first = string.charAt(0);
		final char upper = Character.toUpperCase(first);
		return first == upper ? (string) : (upper + string.substring(1));
	}

	/// @see String#hashCode()
	public static int hashCode(final CharSequence string, final int offset, final int length) {
		Require.checkOffsetLength(offset, length, length(string));

		int hash = 0;
		for (int i = 0; i < length; ++i) {
			hash = 31 * hash + string.charAt(offset + i);
		}
		return hash;
	}

	/// @see String#hashCode()
	public static int hashCode(final CharSequence string) {
		return hashCode(string, 0, length(string));
	}


	/// @see String#hashCode()
	public static int hashCodeIgnoreCase(final CharSequence string, final int offset, final int length) {
		Require.checkOffsetLength(offset, length, length(string));

		int hash = 0;
		for (int i = 0; i < length; ++i) {
			hash = 31 * hash + Character.toUpperCase(string.charAt(offset + i));
		}
		return hash;
	}

	/// @see String#hashCode()
	public static int hashCodeIgnoreCase(final CharSequence string) {
		return hashCodeIgnoreCase(string, 0, length(string));
	}


	public static boolean equals(
			final CharSequence left, final int leftOffset, final int leftLength,
			final CharSequence right, final int rightOffset, final int rightLength
	) {
		Require.checkOffsetLength(leftOffset, leftLength, length(left));
		Require.checkOffsetLength(rightOffset, rightLength, length(right));

		if (leftLength != rightLength) {
			return false;
		}

		for (int i = 0; i < leftLength; ++i) {
			if (left.charAt(leftOffset + i) != right.charAt(rightOffset + i)) {
				return false;
			}
		}

		return true;
	}

	public static boolean equals(
			final CharSequence left, final int leftOffset, final int leftLength,
			final CharSequence right
	) {
		return equals(left, leftOffset, leftLength, right, 0, length(right));
	}

	public static boolean equals(
			final CharSequence left,
			final CharSequence right, final int rightOffset, final int rightLength
	) {
		return equals(left, 0, length(left), right, rightOffset, rightLength);
	}

	public static boolean equals(final CharSequence left, final CharSequence right) {
		return equals(left, 0, length(left), right, 0, length(right));
	}


	public static boolean equalsIgnoreCase(
			final CharSequence left, final int leftOffset, final int leftLength,
			final CharSequence right, final int rightOffset, final int rightLength
	) {
		Require.checkOffsetLength(leftOffset, leftLength, length(left));
		Require.checkOffsetLength(rightOffset, rightLength, length(right));

		if (leftLength != rightLength) {
			return false;
		}

		for (int i = 0; i < leftLength; ++i) {
			if (Character.toUpperCase(left.charAt(leftOffset + i)) != Character.toUpperCase(right.charAt(rightOffset + i))) {
				return false;
			}
		}

		return true;
	}

	public static boolean equalsIgnoreCase(
			final CharSequence left, final int leftOffset, final int leftLength,
			final CharSequence right
	) {
		return equalsIgnoreCase(left, leftOffset, leftLength, right, 0, length(right));
	}

	public static boolean equalsIgnoreCase(
			final CharSequence left,
			final CharSequence right, final int rightOffset, final int rightLength
	) {
		return equalsIgnoreCase(left, 0, length(left), right, rightOffset, rightLength);
	}

	public static boolean equalsIgnoreCase(final CharSequence left, final CharSequence right) {
		return equalsIgnoreCase(left, 0, length(left), right, 0, length(right));
	}


	private Strings() {
		LangUtil.unreachable();
	}

}
