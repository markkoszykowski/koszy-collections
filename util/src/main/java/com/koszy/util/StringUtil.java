package com.koszy.util;

import java.io.IOException;

public final class StringUtil {

	public static final byte NULL_BYTE = 0;
	public static final char NULL_CHAR = 0;

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

	private static int length0(final CharSequence string) {
		return string == null ? 0 : string.length();
	}

	public static int length(final CharSequence string) {
		return string == null ? 0 : Require.isNonNegative(string.length());
	}


	public static void check(final CharSequence string, final int begin, final int end) {
		Require.checkBeginEnd(begin, end, length0(string));
	}

	public static void check(final char[] string, final int offset, final int length) {
		Require.checkOffsetLength(offset, length, ArrayUtil.length(string));
	}


	public static boolean isAscii(final byte b) {
		return 0 <= b;
	}

	public static boolean isAscii(final char c) {
		return c < 0x80;
	}


	public static boolean isLatin1(final byte b) {
		return true;
	}

	public static boolean isLatin1(final char c) {
		return c <= 0xFF;
	}


	private static boolean isAscii0(final CharSequence string, final int begin, final int end) {
		for (int i = begin; i != end; ++i) {
			if (!isAscii(string.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAscii(final CharSequence string, final int begin, final int end) {
		Require.checkBeginEnd(begin, end, length0(string));
		return isAscii0(string, begin, end);
	}

	public static boolean isAscii(final CharSequence string) {
		return isAscii0(string, 0, length(string));
	}


	private static boolean isAscii0(final char[] string, final int offset, final int length) {
		final int begin = offset;
		final int end = begin + length;
		for (int i = begin; i != end; ++i) {
			if (!isAscii(string[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAscii(final char[] string, final int offset, final int length) {
		Require.checkOffsetLength(offset, length, ArrayUtil.length(string));
		return isAscii0(string, offset, length);
	}

	public static boolean isAscii(final char[] string) {
		return isAscii0(string, 0, ArrayUtil.length(string));
	}


	private static boolean isLatin10(final CharSequence string, final int begin, final int end) {
		for (int i = begin; i != end; ++i) {
			if (!isLatin1(string.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLatin1(final CharSequence string, final int begin, final int end) {
		Require.checkBeginEnd(begin, end, length0(string));
		return isLatin10(string, begin, end);
	}

	public static boolean isLatin1(final CharSequence string) {
		return isLatin10(string, 0, length(string));
	}


	private static boolean isLatin10(final char[] string, final int offset, final int length) {
		final int begin = offset;
		final int end = begin + length;
		for (int i = begin; i != end; ++i) {
			if (!isLatin1(string[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLatin1(final char[] string, final int offset, final int length) {
		Require.checkOffsetLength(offset, length, ArrayUtil.length(string));
		return isLatin10(string, offset, length);
	}

	public static boolean isLatin1(final char[] string) {
		return isLatin10(string, 0, ArrayUtil.length(string));
	}


	public static byte toAsciiByte(final char c) {
		Require.isTrue(isAscii(c));
		return (byte) c;
	}

	public static byte toAsciiByte(final char c, final byte defaultt) {
		return isAscii(c) ? (byte) c : defaultt;
	}


	public static byte toLatin1Byte(final char c) {
		Require.isTrue(isLatin1(c));
		return (byte) c;
	}

	public static byte toLatin1Byte(final char c, final byte defaultt) {
		return isLatin1(c) ? (byte) c : defaultt;
	}


	public static char toAsciiChar(final byte b) {
		Require.isTrue(isAscii(b));
		return (char) b;
	}

	public static char toAsciiChar(final byte b, final char defaultt) {
		return isAscii(b) ? (char) b : defaultt;
	}


	public static char toLatin1Char(final byte b) {
		Require.isTrue(isLatin1(b));
		return (char) (b & 0xFF);
	}

	public static char toLatin1Char(final byte b, final char defaultt) {
		return isLatin1(b) ? (char) (b & 0xFF) : defaultt;
	}


	public static char toChar(final CharSequence string) {
		Require.isTrue(string != null && string.length() == 1);
		return string.charAt(0);
	}

	public static <T extends Appendable> T append(final T appendable, final Object object) {
		if (appendable != null && object != null) {
			try {
				appendable.append(object.toString());
			} catch (final IOException exception) {
				LangUtil.rethrow(exception);
			}
		}
		return appendable;
	}

	public static <T extends Appendable> T append(final T appendable, final CharSequence string, final int offset, final int length) {
		if (appendable != null && string != null) {
			try {
				appendable.append(string, offset, offset + length);
			} catch (final IOException exception) {
				LangUtil.rethrow(exception);
			}
		}
		return appendable;
	}

	public static <T extends Appendable> T append(final T appendable, final CharSequence string) {
		if (appendable != null && string != null) {
			try {
				appendable.append(string);
			} catch (final IOException exception) {
				LangUtil.rethrow(exception);
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
	private static int hashCode0(final CharSequence string, final int begin, final int end) {
		int hash = 0;
		for (int i = begin; i != end; ++i) {
			hash = 31 * hash + string.charAt(i);
		}
		return hash;
	}

	/// @see String#hashCode()
	public static int hashCode(final CharSequence string, final int begin, final int end) {
		Require.checkBeginEnd(begin, end, length0(string));
		return hashCode0(string, begin, end);
	}

	/// @see String#hashCode()
	public static int hashCode(final CharSequence string) {
		return hashCode0(string, 0, length(string));
	}


	/// @see String#hashCode()
	private static int hashCodeIgnoreCase0(final CharSequence string, final int begin, final int end) {
		int hash = 0;
		for (int i = begin; i != end; ++i) {
			hash = 31 * hash + Character.toUpperCase(string.charAt(i));
		}
		return hash;
	}

	/// @see String#hashCode()
	public static int hashCodeIgnoreCase(final CharSequence string, final int begin, final int end) {
		Require.checkBeginEnd(begin, end, length0(string));
		return hashCodeIgnoreCase0(string, begin, end);
	}

	/// @see String#hashCode()
	public static int hashCodeIgnoreCase(final CharSequence string) {
		return hashCodeIgnoreCase0(string, 0, length(string));
	}


	private static boolean equals0(
			final CharSequence left, final int leftBegin, final int leftEnd,
			final CharSequence right, final int rightBegin, final int rightEnd
	) {
		if ((leftEnd - leftBegin) != (rightEnd - rightBegin)) {
			return false;
		}

		if ((left == null) != (right == null)) {
			return false;
		}

		for (int i = leftBegin, j = rightBegin; i != leftEnd && j != rightEnd; ++i, ++j) {
			if (left.charAt(i) != right.charAt(j)) {
				return false;
			}
		}

		return true;
	}

	public static boolean equals(
			final CharSequence left, final int leftBegin, final int leftEnd,
			final CharSequence right, final int rightBegin, final int rightEnd
	) {
		Require.checkBeginEnd(leftBegin, leftEnd, length0(left));
		Require.checkBeginEnd(rightBegin, rightEnd, length0(right));
		return equals0(left, leftBegin, leftEnd, right, rightBegin, rightEnd);
	}

	public static boolean equals(
			final CharSequence left, final int leftBegin, final int leftEnd,
			final CharSequence right
	) {
		Require.checkBeginEnd(leftBegin, leftEnd, length0(left));
		return equals0(left, leftBegin, leftEnd, right, 0, length(right));
	}

	public static boolean equals(
			final CharSequence left,
			final CharSequence right, final int rightBegin, final int rightEnd
	) {
		Require.checkBeginEnd(rightBegin, rightEnd, length0(right));
		return equals0(left, 0, length(left), right, rightBegin, rightEnd);
	}

	public static boolean equals(final CharSequence left, final CharSequence right) {
		return equals0(left, 0, length(left), right, 0, length(right));
	}


	public static boolean equalsIgnoreCase0(
			final CharSequence left, final int leftBegin, final int leftEnd,
			final CharSequence right, final int rightBegin, final int rightEnd
	) {
		if ((leftEnd - leftBegin) != (rightEnd - rightBegin)) {
			return false;
		}

		if ((left == null) != (right == null)) {
			return false;
		}

		for (int i = leftBegin, j = rightBegin; i != leftEnd && j != rightEnd; ++i, ++j) {
			if (Character.toUpperCase(left.charAt(i)) != Character.toUpperCase(right.charAt(j))) {
				return false;
			}
		}

		return true;
	}

	public static boolean equalsIgnoreCase(
			final CharSequence left, final int leftBegin, final int leftEnd,
			final CharSequence right, final int rightBegin, final int rightEnd
	) {
		Require.checkBeginEnd(leftBegin, leftEnd, length0(left));
		Require.checkBeginEnd(rightBegin, rightEnd, length0(right));
		return equalsIgnoreCase0(left, leftBegin, leftEnd, right, rightBegin, rightEnd);
	}

	public static boolean equalsIgnoreCase(
			final CharSequence left, final int leftBegin, final int leftEnd,
			final CharSequence right
	) {
		Require.checkBeginEnd(leftBegin, leftEnd, length0(left));
		return equalsIgnoreCase0(left, leftBegin, leftEnd, right, 0, length(right));
	}

	public static boolean equalsIgnoreCase(
			final CharSequence left,
			final CharSequence right, final int rightBegin, final int rightEnd
	) {
		Require.checkBeginEnd(rightBegin, rightEnd, length0(right));
		return equalsIgnoreCase0(left, 0, length(left), right, rightBegin, rightEnd);
	}

	public static boolean equalsIgnoreCase(final CharSequence left, final CharSequence right) {
		return equalsIgnoreCase0(left, 0, length(left), right, 0, length(right));
	}


	private StringUtil() {
		LangUtil.unreachable();
	}

}
