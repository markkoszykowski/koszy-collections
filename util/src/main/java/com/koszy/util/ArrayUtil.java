package com.koszy.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class ArrayUtil {

	public static final boolean[] EMPTY_BOOLEANS = new boolean[0];
	public static final char[] EMPTY_CHARS = new char[0];
	public static final byte[] EMPTY_BYTES = new byte[0];
	public static final short[] EMPTY_SHORTS = new short[0];
	public static final int[] EMPTY_INTS = new int[0];
	public static final long[] EMPTY_LONGS = new long[0];
	public static final float[] EMPTY_FLOATS = new float[0];
	public static final double[] EMPTY_DOUBLES = new double[0];
	public static final Object[] EMPTY_OBJECTS = new Object[0];
	public static final String[] EMPTY_STRINGS = new String[0];

	@SafeVarargs
	public static <T> T[] filter(final T[] empty, final IntFunction<T[]> constructor, final Predicate<? super T> filter, final T... elements) {
		if (elements == null) {
			return empty;
		}

		int count = 0;
		for (final T element : elements) {
			if (filter.test(element)) {
				++count;
			}
		}

		if (count == 0 && empty != null) {
			return empty;
		}

		final T[] filtered = constructor.apply(count);

		int index = 0;
		for (final T element : elements) {
			if (filter.test(element)) {
				filtered[index++] = element;
			}
		}

		return filtered;
	}

	@SafeVarargs
	public static <T> T[] filter(final IntFunction<T[]> constructor, final Predicate<? super T> filter, final T... elements) {
		return filter(null, constructor, filter, elements);
	}

	@SafeVarargs
	public static <T> T[] filterNonNull(final T[] empty, final IntFunction<T[]> constructor, final T... elements) {
		return filter(empty, constructor, FunctionUtil.nonNullPredicate(), elements);
	}

	@SafeVarargs
	public static <T> T[] filterNonNull(final IntFunction<T[]> constructor, final T... elements) {
		return filter(null, constructor, FunctionUtil.nonNullPredicate(), elements);
	}

	@SafeVarargs
	public static <T> T[] concat(final IntFunction<T[]> constructor, final T[]... arrays) {
		if (arrays == null) {
			return null;
		}

		if (arrays.length == 1) {
			return arrays[0];
		}

		int count = 0;
		for (final T[] array : arrays) {
			if (array != null) {
				count += array.length;
			}
		}

		final T[] concat = constructor.apply(count);

		int index = 0;
		for (final T[] array : arrays) {
			final int length = array.length;
			System.arraycopy(array, 0, concat, index, length);
			index += length;
		}

		return concat;
	}

	/// @see java.util.Arrays#hashCode(Object[])
	public static <T> int hash(final ToIntFunction<? super T> hash, final T[] array) {
		Require.notNull(hash);

		if (array == null) {
			return 0;
		}

		int result = 1;
		for (final T element : array) {
			result = 31 * result + hash.applyAsInt(element);
		}
		return result;
	}

	/// @see java.util.Arrays#equals(Object[], Object[])
	public static <T> boolean equals(final BiPredicate<? super T, ? super T> equals, final T[] left, final T[] right) {
		Require.notNull(equals);

		if (left == right) {
			return true;
		}

		if (left == null || right == null) {
			return false;
		}

		final int length = left.length;
		if (right.length != length) {
			return false;
		}

		for (int i = 0; i != length; ++i) {
			if (!equals.test(left[i], right[i])) {
				return false;
			}
		}

		return true;
	}

	public static int deepHashCode(final Object object) {
		if (object instanceof boolean[]) {
			return Arrays.hashCode((boolean[]) object);
		} else if (object instanceof char[]) {
			return Arrays.hashCode((char[]) object);
		} else if (object instanceof byte[]) {
			return Arrays.hashCode((byte[]) object);
		} else if (object instanceof short[]) {
			return Arrays.hashCode((short[]) object);
		} else if (object instanceof int[]) {
			return Arrays.hashCode((int[]) object);
		} else if (object instanceof long[]) {
			return Arrays.hashCode((long[]) object);
		} else if (object instanceof float[]) {
			return Arrays.hashCode((float[]) object);
		} else if (object instanceof double[]) {
			return Arrays.hashCode((double[]) object);
		} else if (object instanceof Object[]) {
			return Arrays.deepHashCode((Object[]) object);
		} else {
			return Objects.hashCode(object);
		}
	}

	public static boolean deepEquals(final Object left, final Object right) {
		return Objects.deepEquals(left, right);
	}


	public static boolean[] copyOf(final boolean[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static char[] copyOf(final char[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static byte[] copyOf(final byte[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static short[] copyOf(final short[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static int[] copyOf(final int[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static long[] copyOf(final long[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static float[] copyOf(final float[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static double[] copyOf(final double[] array, final int length) {
		return Arrays.copyOf(array, length);
	}

	public static <T> T[] copyOf(final IntFunction<T[]> constructor, final T[] array, final int length) {
		final T[] copy = constructor.apply(length);
		System.arraycopy(array, 0, copy, 0, Math.min(length, array.length));
		return copy;
	}


	public static int length(final boolean[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final char[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final byte[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final short[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final int[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final long[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final float[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final double[] array) {
		return array == null ? 0 : array.length;
	}

	public static int length(final Object[] array) {
		return array == null ? 0 : array.length;
	}

	private ArrayUtil() {
		LangUtil.unreachable();
	}

}
