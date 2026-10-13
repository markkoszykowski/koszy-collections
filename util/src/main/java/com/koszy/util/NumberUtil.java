package com.koszy.util;

import org.jspecify.annotations.NonNull;

public final class NumberUtil {

	public static byte byteValue(final @NonNull Object object) throws ClassCastException {
		return ((Number) object).byteValue();
	}

	public static short shortValue(final @NonNull Object object) throws ClassCastException {
		return ((Number) object).shortValue();
	}

	public static int intValue(final @NonNull Object object) throws ClassCastException {
		return ((Number) object).intValue();
	}

	public static long longValue(final @NonNull Object object) throws ClassCastException {
		return ((Number) object).longValue();
	}

	public static float floatValue(final @NonNull Object object) throws ClassCastException {
		return ((Number) object).floatValue();
	}

	public static double doubleValue(final @NonNull Object object) throws ClassCastException {
		return ((Number) object).doubleValue();
	}


	public static short toUnsignedShort(final byte b) {
		return (short) Byte.toUnsignedInt(b);
	}

	public static int toUnsignedInt(final byte b) {
		return Byte.toUnsignedInt(b);
	}

	public static int toUnsignedInt(final short s) {
		return Short.toUnsignedInt(s);
	}

	public static long toUnsignedLong(final byte b) {
		return Byte.toUnsignedLong(b);
	}

	public static long toUnsignedLong(final short s) {
		return Short.toUnsignedLong(s);
	}

	public static long toUnsignedLong(final int i) {
		return Integer.toUnsignedLong(i);
	}


	public static byte toByteExact(final short s) {
		if ((byte) s != s) {
			throw new ArithmeticException("byte overflow");
		}
		return (byte) s;
	}

	public static byte toByteExact(final int i) {
		if ((byte) i != i) {
			throw new ArithmeticException("byte overflow");
		}
		return (byte) i;
	}

	public static byte toByteExact(final long l) {
		if ((byte) l != l) {
			throw new ArithmeticException("byte overflow");
		}
		return (byte) l;
	}

	public static short toShortExact(final int i) {
		if ((short) i != i) {
			throw new ArithmeticException("short overflow");
		}
		return (short) i;
	}

	public static short toShortExact(final long l) {
		if ((short) l != l) {
			throw new ArithmeticException("short overflow");
		}
		return (short) l;
	}

	public static int toIntExact(final long l) {
		return Math.toIntExact(l);
	}


	public static byte toUnsignedByteExact(final short s) {
		if (Byte.toUnsignedInt((byte) s) != s) {
			throw new ArithmeticException("byte overflow");
		}
		return (byte) s;
	}

	public static byte toUnsignedByteExact(final int i) {
		if (Byte.toUnsignedInt((byte) i) != i) {
			throw new ArithmeticException("byte overflow");
		}
		return (byte) i;
	}

	public static byte toUnsignedByteExact(final long l) {
		if (Byte.toUnsignedLong((byte) l) != l) {
			throw new ArithmeticException("byte overflow");
		}
		return (byte) l;
	}

	public static short toUnsignedShortExact(final int i) {
		if (Short.toUnsignedInt((short) i) != i) {
			throw new ArithmeticException("short overflow");
		}
		return (short) i;
	}

	public static short toUnsignedShortExact(final long l) {
		if (Short.toUnsignedInt((short) l) != l) {
			throw new ArithmeticException("short overflow");
		}
		return (short) l;
	}

	public static int toUnsignedIntExact(final long l) {
		if (Integer.toUnsignedLong((int) l) != l) {
			throw new ArithmeticException("short overflow");
		}
		return (int) l;
	}


	public static int align(final int x, final int alignment) {
		return (x + alignment - 1) & -alignment;
	}

	public static long align(final long x, final int alignment) {
		return (x + alignment - 1L) & -alignment;
	}


	public static short s(final byte b0, final byte b1) {
		return (short) ((Byte.toUnsignedInt(b0) << Byte.SIZE) | Byte.toUnsignedInt(b1));
	}

	public static int i(final byte b0, final byte b1, final byte b2, final byte b3) {
		return (Byte.toUnsignedInt(b0) << (3 * Byte.SIZE)) | (Byte.toUnsignedInt(b1) << (2 * Byte.SIZE)) | (Byte.toUnsignedInt(b2) << Byte.SIZE) | Byte.toUnsignedInt(b3);
	}

	public static int i(final short s0, final short s1) {
		return (Short.toUnsignedInt(s0) << Short.SIZE) | Short.toUnsignedInt(s1);
	}

	public static long l(
			final byte b0, final byte b1, final byte b2, final byte b3,
			final byte b4, final byte b5, final byte b6, final byte b7
	) {
		return (Byte.toUnsignedLong(b0) << (7 * Byte.SIZE)) | (Byte.toUnsignedLong(b1) << (6 * Byte.SIZE)) | (Byte.toUnsignedLong(b2) << (5 * Byte.SIZE)) | (Byte.toUnsignedLong(b3) << (4 * Byte.SIZE)) |
				(Byte.toUnsignedLong(b4) << (3 * Byte.SIZE)) | (Byte.toUnsignedLong(b5) << (2 * Byte.SIZE)) | (Byte.toUnsignedLong(b6) << Byte.SIZE) | Byte.toUnsignedLong(b7);
	}

	public static long l(final short s0, final short s1, final short s2, final short s3) {
		return (Short.toUnsignedLong(s0) << (3 * Short.SIZE)) | (Short.toUnsignedLong(s1) << (2 * Short.SIZE)) | (Short.toUnsignedLong(s2) << Short.SIZE) | Short.toUnsignedLong(s3);
	}

	public static long l(final int i0, final int i1) {
		return (Integer.toUnsignedLong(i0) << Integer.SIZE) | Integer.toUnsignedLong(i1);
	}


	public static byte b0(final short s) {
		return (byte) (s >>> Byte.SIZE);
	}

	public static byte b0(final int i) {
		return (byte) (i >>> (Byte.SIZE * 3));
	}

	public static byte b0(final long l) {
		return (byte) (l >>> (Byte.SIZE * 7));
	}

	public static byte b1(final short s) {
		return (byte) s;
	}

	public static byte b1(final int i) {
		return (byte) (i >>> (Byte.SIZE * 2));
	}

	public static byte b1(final long l) {
		return (byte) (l >>> (Byte.SIZE * 6));
	}

	public static byte b2(final int i) {
		return (byte) (i >>> Byte.SIZE);
	}

	public static byte b2(final long l) {
		return (byte) (l >>> (Byte.SIZE * 5));
	}

	public static byte b3(final int i) {
		return (byte) i;
	}

	public static byte b3(final long l) {
		return (byte) (l >>> (Byte.SIZE * 4));
	}

	public static byte b4(final long l) {
		return (byte) (l >>> (Byte.SIZE * 3));
	}

	public static byte b5(final long l) {
		return (byte) (l >>> (Byte.SIZE * 2));
	}

	public static byte b6(final long l) {
		return (byte) (l >>> Byte.SIZE);
	}

	public static byte b7(final long l) {
		return (byte) l;
	}

	public static short s0(final int i) {
		return (short) (i >>> Short.SIZE);
	}

	public static short s0(final long l) {
		return (short) (l >>> (Short.SIZE * 3));
	}

	public static short s1(final int i) {
		return (short) i;
	}

	public static short s1(final long l) {
		return (short) (l >>> (Short.SIZE * 2));
	}

	public static short s2(final long l) {
		return (short) (l >>> Short.SIZE);
	}

	public static short s3(final long l) {
		return (short) l;
	}

	public static int i0(final long l) {
		return (int) (l >>> Integer.SIZE);
	}

	public static int i1(final long l) {
		return (int) l;
	}


	public static int add(final int a, final int b) {
		return a + b;
	}

	public static long add(final long a, final long b) {
		return a + b;
	}

	public static int subtract(final int a, final int b) {
		return a - b;
	}

	public static long subtract(final long a, final long b) {
		return a - b;
	}

	public static int multiply(final int a, final int b) {
		return a * b;
	}

	public static long multiply(final long a, final long b) {
		return a * b;
	}

	public static int divide(final int a, final int b) {
		return a / b;
	}

	public static long divide(final long a, final long b) {
		return a / b;
	}


	public static int max(final int x1, final int x2) {
		return Math.max(x1, x2);
	}

	public static int max(final int x1, final int x2, final int x3) {
		return Math.max(x1, Math.max(x2, x3));
	}

	public static int max(final int x1, final int x2, final int x3, final int x4) {
		return Math.max(x1, Math.max(x2, Math.max(x3, x4)));
	}

	public static int max(final int x1, final int x2, final int x3, final int x4, final int x5) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, x5))));
	}

	public static int max(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, x6)))));
	}

	public static int max(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, x7))))));
	}

	public static int max(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7, final int x8
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, Math.max(x7, x8)))))));
	}

	public static int max(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7, final int x8, final int x9
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, Math.max(x7, Math.max(x8, x9))))))));
	}

	public static int max(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7, final int x8, final int x9, final int x10
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, Math.max(x7, Math.max(x8, Math.max(x9, x10)))))))));
	}

	public static int max(final int x0, final int x1, final int... xs) {
		int max = Math.max(x0, x1);
		if (xs != null) {
			for (final int x : xs) {
				max = Math.max(max, x);
			}
		}
		return max;
	}


	public static long max(final long x1, final long x2) {
		return Math.max(x1, x2);
	}

	public static long max(final long x1, final long x2, final long x3) {
		return Math.max(x1, Math.max(x2, x3));
	}

	public static long max(final long x1, final long x2, final long x3, final long x4) {
		return Math.max(x1, Math.max(x2, Math.max(x3, x4)));
	}

	public static long max(final long x1, final long x2, final long x3, final long x4, final long x5) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, x5))));
	}

	public static long max(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, x6)))));
	}

	public static long max(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, x7))))));
	}

	public static long max(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7, final long x8
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, Math.max(x7, x8)))))));
	}

	public static long max(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7, final long x8, final long x9
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, Math.max(x7, Math.max(x8, x9))))))));
	}

	public static long max(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7, final long x8, final long x9, final long x10
	) {
		return Math.max(x1, Math.max(x2, Math.max(x3, Math.max(x4, Math.max(x5, Math.max(x6, Math.max(x7, Math.max(x8, Math.max(x9, x10)))))))));
	}

	public static long max(final long x0, final long x1, final long... xs) {
		long max = Math.max(x0, x1);
		if (xs != null) {
			for (final long x : xs) {
				max = Math.max(max, x);
			}
		}
		return max;
	}


	public static int min(final int x1, final int x2) {
		return Math.min(x1, x2);
	}

	public static int min(final int x1, final int x2, final int x3) {
		return Math.min(x1, Math.min(x2, x3));
	}

	public static int min(final int x1, final int x2, final int x3, final int x4) {
		return Math.min(x1, Math.min(x2, Math.min(x3, x4)));
	}

	public static int min(final int x1, final int x2, final int x3, final int x4, final int x5) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, x5))));
	}

	public static int min(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, x6)))));
	}

	public static int min(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, x7))))));
	}

	public static int min(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7, final int x8
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, Math.min(x7, x8)))))));
	}

	public static int min(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7, final int x8, final int x9
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, Math.min(x7, Math.min(x8, x9))))))));
	}

	public static int min(
			final int x1, final int x2, final int x3, final int x4, final int x5,
			final int x6, final int x7, final int x8, final int x9, final int x10
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, Math.min(x7, Math.min(x8, Math.min(x9, x10)))))))));
	}

	public static int min(final int x0, final int x1, final int... xs) {
		int min = Math.min(x0, x1);
		if (xs != null) {
			for (final int x : xs) {
				min = Math.min(min, x);
			}
		}
		return min;
	}


	public static long min(final long x1, final long x2) {
		return Math.min(x1, x2);
	}

	public static long min(final long x1, final long x2, final long x3) {
		return Math.min(x1, Math.min(x2, x3));
	}

	public static long min(final long x1, final long x2, final long x3, final long x4) {
		return Math.min(x1, Math.min(x2, Math.min(x3, x4)));
	}

	public static long min(final long x1, final long x2, final long x3, final long x4, final long x5) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, x5))));
	}

	public static long min(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, x6)))));
	}

	public static long min(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, x7))))));
	}

	public static long min(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7, final long x8
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, Math.min(x7, x8)))))));
	}

	public static long min(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7, final long x8, final long x9
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, Math.min(x7, Math.min(x8, x9))))))));
	}

	public static long min(
			final long x1, final long x2, final long x3, final long x4, final long x5,
			final long x6, final long x7, final long x8, final long x9, final long x10
	) {
		return Math.min(x1, Math.min(x2, Math.min(x3, Math.min(x4, Math.min(x5, Math.min(x6, Math.min(x7, Math.min(x8, Math.min(x9, x10)))))))));
	}

	public static long min(final long x0, final long x1, final long... xs) {
		long min = Math.min(x0, x1);
		if (xs != null) {
			for (final long x : xs) {
				min = Math.min(min, x);
			}
		}
		return min;
	}


	private NumberUtil() {
		LangUtil.unreachable();
	}

}
