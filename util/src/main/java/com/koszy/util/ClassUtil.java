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
		for (int c = 0; c != 100_000; ++c) {
			final Object o1 = instantiate.apply(random.nextInt(5));
			final Object o2 = instantiate.apply(random.nextInt(5));
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

	private static final class HeaderSize {
		private byte b;
	}

	private static final class ObjectAlignment0 {
	}

	private static final class ObjectAlignment1 {
		private boolean b;
	}

	private static final class ObjectAlignment2 {
		private int i;
	}

	private static final class ObjectAlignment3 {
		private long l;
	}

	private static final class ObjectAlignment4 {
		private Object o;
	}

	private ClassUtil() {
		LangUtil.unreachable();
	}

}
