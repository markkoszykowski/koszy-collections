package com.koszy.util;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.ToLongFunction;

public final class ClassUtil {

	private static final int OBJECT_HEADER_SIZE = objectHeaderSize0();
	private static final int OBJECT_ALIGNMENT = objectAlignment0();

	public static String name(final Class<?> clazz) {
		String name;
		if (clazz == null) {
			return null;
		} else if (!(name = clazz.getSimpleName()).isEmpty()) {
			return name;
		} else if ((name = clazz.getCanonicalName()) != null) {
			return name;
		} else {
			return clazz.getTypeName();
		}
	}


	public static Class<?> typeof(final boolean value) {
		return boolean.class;
	}

	public static Class<?> typeof(final char value) {
		return char.class;
	}

	public static Class<?> typeof(final byte value) {
		return byte.class;
	}

	public static Class<?> typeof(final short value) {
		return short.class;
	}

	public static Class<?> typeof(final int value) {
		return int.class;
	}

	public static Class<?> typeof(final long value) {
		return long.class;
	}

	public static Class<?> typeof(final float value) {
		return float.class;
	}

	public static Class<?> typeof(final double value) {
		return double.class;
	}

	public static Class<?> typeof(final Object object) {
		return object == null ? null : object.getClass();
	}


	public static boolean isBoolean(final Class<?> clazz) {
		return Objects.equals(boolean.class, clazz) || Objects.equals(Boolean.class, clazz);
	}

	public static boolean isChar(final Class<?> clazz) {
		return Objects.equals(char.class, clazz) || Objects.equals(Character.class, clazz);
	}

	public static boolean isByte(final Class<?> clazz) {
		return Objects.equals(byte.class, clazz) || Objects.equals(Byte.class, clazz);
	}

	public static boolean isShort(final Class<?> clazz) {
		return Objects.equals(short.class, clazz) || Objects.equals(Short.class, clazz);
	}

	public static boolean isInt(final Class<?> clazz) {
		return Objects.equals(int.class, clazz) || Objects.equals(Integer.class, clazz);
	}

	public static boolean isLong(final Class<?> clazz) {
		return Objects.equals(long.class, clazz) || Objects.equals(Long.class, clazz);
	}

	public static boolean isFloat(final Class<?> clazz) {
		return Objects.equals(float.class, clazz) || Objects.equals(Float.class, clazz);
	}

	public static boolean isDouble(final Class<?> clazz) {
		return Objects.equals(double.class, clazz) || Objects.equals(Double.class, clazz);
	}

	public static boolean assignable(final Class<?> excepted, final Class<?> actual) {
		if (Objects.equals(excepted, actual)) {
			return true;
		}
		if (excepted == null || actual == null) {
			return true;
		}
		if (isBoolean(excepted) && isBoolean(actual)) {
			return true;
		}
		if (isChar(excepted) && isChar(actual)) {
			return true;
		}
		if (isByte(excepted) && isByte(actual)) {
			return true;
		}
		if (isShort(excepted) && isShort(actual)) {
			return true;
		}
		if (isInt(excepted) && isInt(actual)) {
			return true;
		}
		if (isLong(excepted) && isLong(actual)) {
			return true;
		}
		if (isFloat(excepted) && isFloat(actual)) {
			return true;
		}
		if (isDouble(excepted) && isDouble(actual)) {
			return true;
		}
		return excepted.isAssignableFrom(actual);
	}

	public static boolean assignable(final Class<?> clazz, final boolean value) {
		return isBoolean(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final char value) {
		return isChar(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final byte value) {
		return isByte(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final short value) {
		return isShort(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final int value) {
		return isInt(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final long value) {
		return isLong(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final float value) {
		return isFloat(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final double value) {
		return isDouble(clazz);
	}

	public static boolean assignable(final Class<?> clazz, final Object object) {
		return assignable(clazz, typeof(object));
	}

	private static int objectHeaderSize0() {
		try {
			return NumberUtil.toIntExact(OpenUnsafe.theUnsafe().objectFieldOffset(HeaderSize.class.getDeclaredField("b")));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static int objectAlignment0() {
		try {
			/// @see org.openjdk.jol.vm.VMOptions#pollObjectAlignment()
			final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			final ObjectName mbean = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
			final Function<String, String> getString = key -> {
				try {
					return ((CompositeDataSupport) server.invoke(mbean, "getVMOption", new Object[]{"UseCompressedOops"}, new String[]{String.class.getName()})).get("value").toString();
				} catch (final Exception exception) {
					return LangUtil.rethrow(exception);
				}
			};
			if (Objects.equals(Boolean.TRUE, Boolean.parseBoolean(getString.apply("UseCompressedOops")))) {
				return Integer.parseInt(getString.apply("ObjectAlignmentInBytes"));
			}
		} catch (final Exception exception) {
		}

		/// @see org.openjdk.jol.vm.HotspotUnsafe#guessAlignment()
		long min = -1L;
		final Random random = new Random();
		final Object[] array = new Object[1];
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final ToLongFunction<Object> address = o -> {
			array[0] = o;
			if (OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE == OpenUnsafe.ARRAY_BYTE_INDEX_SCALE) {
				return Byte.toUnsignedLong(unsafe.getByte(array, OpenUnsafe.ARRAY_OBJECT_BASE_OFFSET));
			} else if (OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE == OpenUnsafe.ARRAY_SHORT_INDEX_SCALE) {
				return Short.toUnsignedLong(unsafe.getShort(array, OpenUnsafe.ARRAY_OBJECT_BASE_OFFSET));
			} else if (OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE == OpenUnsafe.ARRAY_INT_INDEX_SCALE) {
				return Integer.toUnsignedLong(unsafe.getInt(array, OpenUnsafe.ARRAY_OBJECT_BASE_OFFSET));
			} else if (OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE == OpenUnsafe.ARRAY_LONG_INDEX_SCALE) {
				return unsafe.getLong(array, OpenUnsafe.ARRAY_OBJECT_BASE_OFFSET);
			} else {
				throw new Error("unsupported address size: " + OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE);
			}
		};
		final IntFunction<Object> instantiate = i -> {
			switch (i) {
				case 0:
					return new ObjectAlignment0();
				case 1:
					return new ObjectAlignment1();
				case 2:
					return new ObjectAlignment2();
				case 3:
					return new ObjectAlignment3();
				case 4:
					return new ObjectAlignment4();
				case 5:
					return new ObjectAlignment5();
				case 6:
					return new ObjectAlignment6();
				case 7:
					return new ObjectAlignment7();
				default:
					throw new IllegalStateException();
			}
		};
		final LongBinaryOperator gcd = (a, b) -> {
			long x = a;
			long y = b;
			while (0L < y) {
				final long temp = y;
				y = x % y;
				x = temp;
			}
			return x;
		};
		for (int c = 0; c != (1L << 16); ++c) {
			final Object o1 = instantiate.apply(random.nextInt(8));
			final Object o2 = instantiate.apply(random.nextInt(8));
			final long diff = Math.abs(address.applyAsLong(o1) - address.applyAsLong(o2));
			min = min == -1L ? diff : gcd.applyAsLong(min, diff);
		}
		return (int) min;
	}

	public static int objectHeaderSize() {
		return OBJECT_HEADER_SIZE;
	}

	public static int objectAlignment() {
		return OBJECT_ALIGNMENT;
	}

	private static long sizeof(final Field field) {
		final Class<?> type = field.getType();
		if (Objects.equals(boolean.class, type)) {
			return OpenUnsafe.ARRAY_BOOLEAN_INDEX_SCALE;
		} else if (Objects.equals(byte.class, type)) {
			return OpenUnsafe.ARRAY_BYTE_INDEX_SCALE;
		} else if (Objects.equals(short.class, type)) {
			return OpenUnsafe.ARRAY_SHORT_INDEX_SCALE;
		} else if (Objects.equals(char.class, type)) {
			return OpenUnsafe.ARRAY_CHAR_INDEX_SCALE;
		} else if (Objects.equals(int.class, type)) {
			return OpenUnsafe.ARRAY_INT_INDEX_SCALE;
		} else if (Objects.equals(long.class, type)) {
			return OpenUnsafe.ARRAY_LONG_INDEX_SCALE;
		} else if (Objects.equals(float.class, type)) {
			return OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE;
		} else if (Objects.equals(double.class, type)) {
			return OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE;
		} else {
			return OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE;
		}
	}

	public static long sizeof(final Class<?> clazz) {
		long size;
		if (clazz.isArray()) {
			final Class<?> component = clazz.getComponentType();
			if (Objects.equals(boolean.class, component)) {
				size = OpenUnsafe.ARRAY_BOOLEAN_BASE_OFFSET;
			} else if (Objects.equals(byte.class, component)) {
				size = OpenUnsafe.ARRAY_BYTE_BASE_OFFSET;
			} else if (Objects.equals(short.class, component)) {
				size = OpenUnsafe.ARRAY_SHORT_BASE_OFFSET;
			} else if (Objects.equals(char.class, component)) {
				size = OpenUnsafe.ARRAY_CHAR_BASE_OFFSET;
			} else if (Objects.equals(int.class, component)) {
				size = OpenUnsafe.ARRAY_INT_BASE_OFFSET;
			} else if (Objects.equals(long.class, component)) {
				size = OpenUnsafe.ARRAY_LONG_BASE_OFFSET;
			} else if (Objects.equals(float.class, component)) {
				size = OpenUnsafe.ARRAY_FLOAT_BASE_OFFSET;
			} else if (Objects.equals(double.class, component)) {
				size = OpenUnsafe.ARRAY_DOUBLE_BASE_OFFSET;
			} else {
				size = OpenUnsafe.ARRAY_OBJECT_BASE_OFFSET;
			}
		} else {
			size = OBJECT_HEADER_SIZE;
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			for (final Field field : clazz.getFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					final long offset = unsafe.objectFieldOffset(field);
					size = Math.max(size, offset + sizeof(field));
				}
			}
			for (final Field field : clazz.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					final long offset = unsafe.objectFieldOffset(field);
					size = Math.max(size, offset + sizeof(field));
				}
			}
		}
		return NumberUtil.align(size, OBJECT_ALIGNMENT);
	}

	public static Field[] fields(final Class<?> clazz) {
		return clazz == null ? null : ArrayUtil.concat(Field[]::new, clazz.getDeclaredFields(), clazz.getFields());
	}

	private static final class HeaderSize {
		private byte b;
	}

	private static final class ObjectAlignment0 {
	}

	private static final class ObjectAlignment1 {
		private boolean b;
	}

	private static final class ObjectAlignment2 {
		private char c;
	}

	private static final class ObjectAlignment3 {
		private byte b;
	}

	private static final class ObjectAlignment4 {
		private short s;
	}

	private static final class ObjectAlignment5 {
		private int i;
	}

	private static final class ObjectAlignment6 {
		private long l;
	}

	private static final class ObjectAlignment7 {
		private Object o;
	}

	private ClassUtil() {
		LangUtil.unreachable();
	}

}
