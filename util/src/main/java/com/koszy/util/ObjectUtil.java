package com.koszy.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ObjectUtil {

	private static final Method CLONE = cloneMethod();

	private static Method cloneMethod() {
		try {
			final Method clone = Object.class.getDeclaredMethod("clone");
			clone.setAccessible(true);
			return clone;
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static boolean clone(final boolean value) {
		return value;
	}

	public static char clone(final char value) {
		return value;
	}

	public static byte clone(final byte value) {
		return value;
	}

	public static short clone(final short value) {
		return value;
	}

	public static int clone(final int value) {
		return value;
	}

	public static long clone(final long value) {
		return value;
	}

	public static float clone(final float value) {
		return value;
	}

	public static double clone(final double value) {
		return value;
	}

	public static boolean[] clone(final boolean[] object) {
		return object == null ? null : object.clone();
	}

	public static char[] clone(final char[] object) {
		return object == null ? null : object.clone();
	}

	public static byte[] clone(final byte[] object) {
		return object == null ? null : object.clone();
	}

	public static short[] clone(final short[] object) {
		return object == null ? null : object.clone();
	}

	public static int[] clone(final int[] object) {
		return object == null ? null : object.clone();
	}

	public static long[] clone(final long[] object) {
		return object == null ? null : object.clone();
	}

	public static float[] clone(final float[] object) {
		return object == null ? null : object.clone();
	}

	public static double[] clone(final double[] object) {
		return object == null ? null : object.clone();
	}

	public static <T> T[] clone(final T[] object) {
		return object == null ? null : object.clone();
	}

	public static <T> T clone(final T object) {
		return clone(object, false);
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> T clone(final T object, final boolean force) {
		if (object == null) {
			return null;
		}
		try {
			if (force) {
				return (T) CLONE.invoke(object);
			}
			final Class<?> clazz = object.getClass();
			if (!Cloneable.class.isAssignableFrom(clazz)) {
				return LangUtil.rethrow(new CloneNotSupportedException(ArrayUtil.deepToString(object)));
			}
			final Method method = clazz.getMethod("clone");
			if (!Modifier.isPublic(method.getModifiers())) {
				return LangUtil.rethrow(new CloneNotSupportedException(ArrayUtil.deepToString(object)));
			}
			return (T) method.invoke(object);
		} catch (final InvocationTargetException exception) {
			return LangUtil.rethrow(exception.getTargetException());
		} catch (final ReflectiveOperationException exception) {
			final CloneNotSupportedException unsupported = new CloneNotSupportedException(ArrayUtil.deepToString(object));
			unsupported.addSuppressed(exception);
			return LangUtil.rethrow(unsupported);
		}
	}

	private ObjectUtil() {
		LangUtil.unreachable();
	}

}
