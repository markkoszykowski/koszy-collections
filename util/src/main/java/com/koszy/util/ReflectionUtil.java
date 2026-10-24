package com.koszy.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtil {

	static final Class<?>[] NO_ARGS = new Class[0];
	static final Constructor<?>[] NO_CONSTRUCTORS = new Constructor[0];

	public static <T extends AccessibleObject> T access(final T object) {
		object.setAccessible(true);
		return object;
	}

	public static boolean getBoolean(final Field field) {
		try {
			return field.getBoolean(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static boolean getBoolean(final Object object, final Field field) {
		try {
			return field.getBoolean(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static char getChar(final Field field) {
		try {
			return field.getChar(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static char getChar(final Object object, final Field field) {
		try {
			return field.getChar(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static byte getByte(final Field field) {
		try {
			return field.getByte(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static byte getByte(final Object object, final Field field) {
		try {
			return field.getByte(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static short getShort(final Field field) {
		try {
			return field.getShort(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static short getShort(final Object object, final Field field) {
		try {
			return field.getShort(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static int getInt(final Field field) {
		try {
			return field.getInt(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static int getInt(final Object object, final Field field) {
		try {
			return field.getInt(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static long getLong(final Field field) {
		try {
			return field.getLong(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static long getLong(final Object object, final Field field) {
		try {
			return field.getLong(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static float getFloat(final Field field) {
		try {
			return field.getFloat(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static float getFloat(final Object object, final Field field) {
		try {
			return field.getFloat(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static double getDouble(final Field field) {
		try {
			return field.getDouble(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static double getDouble(final Object object, final Field field) {
		try {
			return field.getDouble(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> T get(final Field field) {
		try {
			return (T) field.get(null);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> T get(final Object object, final Field field) {
		try {
			return (T) field.get(object);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> T invoke(final Object object, final Method method, final Object... args) {
		try {
			return (T) method.invoke(object, args);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> T invoke(final Method method, final Object... args) {
		try {
			return (T) method.invoke(null, args);
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	public static Class<?>[] types(final Object... args) {
		if (args == null || args.length == 0) {
			return NO_ARGS;
		}
		final Class<?>[] types = new Class[args.length];
		for (int i = 0; i != args.length; ++i) {
			types[i] = ClassUtil.typeof(args[i]);
		}
		return types;
	}

	public static boolean assignable(final Class<?>[] parameters, final Object[] args) {
		final int length = ArrayUtil.length(parameters);
		if (ArrayUtil.length(args) != length) {
			return false;
		}
		for (int i = 0; i != length; ++i) {
			if (!ClassUtil.assignable(parameters[i], args[i])) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Constructor<T>[] constructors(final Class<? extends T> clazz, final Object... args) {
		Constructor<T>[] constructors = (Constructor<T>[]) NO_CONSTRUCTORS;
		for (final Constructor<?> constructor : clazz.getConstructors()) {
			if (assignable(constructor.getParameterTypes(), args)) {
				constructors = ArrayUtil.copyOf(Constructor[]::new, constructors, constructors.length + 1);
				constructors[constructors.length - 1] = (Constructor<T>) constructor;
			}
		}
		return constructors;
	}

	public static <T> T construct(final Class<? extends T> clazz, final Object... args) {
		final Constructor<T>[] constructors = constructors(clazz, args);
		switch (constructors.length) {
			case 0:
				throw new IllegalArgumentException();
			case 1:
				try {
					return constructors[0].newInstance(args);
				} catch (final ReflectiveOperationException exception) {
					return LangUtil.rethrow(exception);
				}
			default:
				Throwable throwable = null;
				for (final Constructor<T> constructor : constructors) {
					try {
						return constructor.newInstance(args);
					} catch (final InvocationTargetException exception) {
						if (throwable instanceof InvocationTargetException) {
							throwable.addSuppressed(exception);
						} else {
							throwable = exception;
						}
					} catch (final ReflectiveOperationException exception) {
						if (throwable == null) {
							throwable = exception;
						} else if (!(throwable instanceof InvocationTargetException)) {
							throwable.addSuppressed(exception);
						}
					}
				}
				return LangUtil.rethrow(throwable);
		}
	}

	private ReflectionUtil() {
		LangUtil.unreachable();
	}

}
