package com.koszy.util;

import org.jspecify.annotations.NonNull;

public final class Numbers {

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

	private Numbers() {
		LangUtil.unreachable();
	}

}
