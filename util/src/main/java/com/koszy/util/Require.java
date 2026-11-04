package com.koszy.util;

public final class Require {

	public static <T> T isNull(final T t) {
		if (t != null) {
			throw new RequirementNotSatisfiedException(ArrayUtil.deepToString(t));
		}
		return t;
	}

	public static <T> T nonNull(final T t) {
		if (t == null) {
			throw new RequirementNotSatisfiedException(ArrayUtil.deepToString(t));
		}
		return t;
	}


	public static boolean isTrue(final boolean bool) {
		if (!bool) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(bool));
		}
		return bool;
	}

	public static boolean isFalse(final boolean bool) {
		if (bool) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(bool));
		}
		return bool;
	}

	public static boolean notTrue(final boolean bool) {
		if (bool) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(bool));
		}
		return bool;
	}

	public static boolean notFalse(final boolean bool) {
		if (!bool) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(bool));
		}
		return bool;
	}


	public static int isNonZero(final int i) {
		if (i == 0) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(i));
		}
		return i;
	}

	public static long isNonZero(final long l) {
		if (l == 0L) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(l));
		}
		return l;
	}

	public static int isZero(final int i) {
		if (i != 0) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(i));
		}
		return i;
	}

	public static long isZero(final long l) {
		if (l != 0L) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(l));
		}
		return l;
	}

	public static int isPositive(final int i) {
		if (i < 0) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(i));
		}
		return i;
	}

	public static long isPositive(final long l) {
		if (l < 0L) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(l));
		}
		return l;
	}

	public static int isNegative(final int i) {
		if (0 < i) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(i));
		}
		return i;
	}

	public static long isNegative(final long l) {
		if (0L < l) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(l));
		}
		return l;
	}


	public static int isNonPositive(final int i) {
		if (0 < i) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(i));
		}
		return i;
	}

	public static long isNonPositive(final long l) {
		if (0L < l) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(l));
		}
		return l;
	}


	public static int isNonNegative(final int i) {
		if (i < 0) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(i));
		}
		return i;
	}

	public static long isNonNegative(final long l) {
		if (l < 0L) {
			throw new RequirementNotSatisfiedException(StringPool.valueOf(l));
		}
		return l;
	}


	/// @see java.util.Objects#checkIndex(int, int)
	public static void checkIndex(final int index, final int length) {
		if (index < 0 || length <= index) {
			throw new RequirementNotSatisfiedException("Index " + index + " out of bounds for length " + length);
		}
	}

	/// @see java.util.Objects#checkIndex(long, long)
	public static void checkIndex(final long index, final long length) {
		if (index < 0L || length <= index) {
			throw new RequirementNotSatisfiedException("Index " + index + " out of bounds for length " + length);
		}
	}

	/// @see java.util.Objects#checkFromToIndex(int, int, int)
	public static void checkBeginEnd(final int begin, final int end, final int length) {
		if (begin < 0 || end < begin || length < end) {
			throw new RequirementNotSatisfiedException("Range[ " + begin + ", " + end + ") out of bounds for length " + length);
		}
	}

	/// @see java.util.Objects#checkFromToIndex(long, long, long)
	public static void checkBeginEnd(final long begin, final long end, final long length) {
		if (begin < 0L || end < begin || length < end) {
			throw new RequirementNotSatisfiedException("Range[ " + begin + ", " + end + ") out of bounds for length " + length);
		}
	}

	/// @see java.util.Objects#checkFromIndexSize(int, int, int)
	public static void checkOffsetLength(final int offset, final int length, final int capacity) {
		if ((offset | length | capacity) < 0 || (capacity - offset) < length) {
			throw new RequirementNotSatisfiedException("Range[ " + offset + ", " + offset + " + " + length + ") out of bounds for length " + capacity);
		}
	}

	/// @see java.util.Objects#checkFromIndexSize(long, long, long)
	public static void checkOffsetLength(final long offset, final long length, final long capacity) {
		if ((offset | length | capacity) < 0L || (capacity - offset) < length) {
			throw new RequirementNotSatisfiedException("Range[ " + offset + ", " + offset + " + " + length + ") out of bounds for length " + capacity);
		}
	}


	private Require() {
		LangUtil.unreachable();
	}

}
