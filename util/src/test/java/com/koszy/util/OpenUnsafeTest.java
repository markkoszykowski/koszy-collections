package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

class OpenUnsafeTest {

	static final int SAMPLES = 1 << 8;

	static boolean randomBoolean(final Random random) {
		return random.nextBoolean();
	}

	static char randomChar(final Random random) {
		return (char) random.nextInt(Character.MAX_VALUE + 1);
	}

	static byte randomByte(final Random random) {
		return (byte) (random.nextInt(Byte.MAX_VALUE - Byte.MIN_VALUE + 1) + Byte.MIN_VALUE);
	}

	static short randomShort(final Random random) {
		return (short) (random.nextInt(Short.MAX_VALUE - Short.MIN_VALUE + 1) + Short.MIN_VALUE);
	}

	static int randomInt(final Random random) {
		return random.nextInt();
	}

	static long randomLong(final Random random) {
		return random.nextLong();
	}

	static float randomFloat(final Random random) {
		return random.nextFloat();
	}

	static double randomDouble(final Random random) {
		return random.nextDouble();
	}

	@Test
	void test() {
		Assertions.assertNotNull(Assertions.assertDoesNotThrow(OpenUnsafe::theUnsafe));

		Assertions.assertDoesNotThrow(() -> OpenUnsafe.INVALID_FIELD_OFFSET);

		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_BOOLEAN_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_BYTE_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_SHORT_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_CHAR_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_INT_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_LONG_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_FLOAT_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_DOUBLE_BASE_OFFSET);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_OBJECT_BASE_OFFSET);

		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_BOOLEAN_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_BYTE_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_SHORT_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_CHAR_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_INT_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_LONG_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE);

		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ADDRESS_SIZE);

		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		Assertions.assertDoesNotThrow(unsafe::unalignedAccess);

		final Random random = new Random();


		{
			final boolean[] array = new boolean[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final boolean value = randomBoolean(random);
				unsafe.putBoolean(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getBoolean(array, 0L));
			}
		}

		{
			final char[] array = new char[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final char value = randomChar(random);
				unsafe.putChar(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getChar(array, 0L));
				unsafe.putCharUnaligned(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getCharUnaligned(array, 0L));
			}
		}

		{
			final byte[] array = new byte[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final byte value = randomByte(random);
				unsafe.putByte(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getByte(array, 0L));
			}
		}

		{
			final short[] array = new short[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final short value = randomShort(random);
				unsafe.putShort(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getShort(array, 0L));
				unsafe.putShortUnaligned(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getShortUnaligned(array, 0L));
			}
		}

		{
			final int[] array = new int[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final int value = randomInt(random);
				unsafe.putInt(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getInt(array, 0L));
				unsafe.putIntUnaligned(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getIntUnaligned(array, 0L));
			}
		}

		{
			final long[] array = new long[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final long value = randomLong(random);
				unsafe.putLong(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getLong(array, 0L));
				unsafe.putLongUnaligned(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getLongUnaligned(array, 0L));
			}
		}

		{
			final float[] array = new float[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final float value = randomFloat(random);
				unsafe.putFloat(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getFloat(array, 0L));
			}
		}

		{
			final double[] array = new double[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final double value = randomDouble(random);
				unsafe.putDouble(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getDouble(array, 0L));
			}
		}

		{
			final Object[] array = new Object[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final Object value = new Object();
				unsafe.putObject(array, 0L, value);
				Assertions.assertSame(value, unsafe.getObject(array, 0L));
			}
		}


		{
			final long address = unsafe.allocateMemory(Character.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final char value = randomChar(random);
					unsafe.putChar(address, value);
					Assertions.assertEquals(value, unsafe.getChar(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Byte.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final byte value = randomByte(random);
					unsafe.putByte(address, value);
					Assertions.assertEquals(value, unsafe.getByte(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Short.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final short value = randomShort(random);
					unsafe.putShort(address, value);
					Assertions.assertEquals(value, unsafe.getShort(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Integer.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final int value = randomInt(random);
					unsafe.putInt(address, value);
					Assertions.assertEquals(value, unsafe.getInt(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Long.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final long value = randomLong(random);
					unsafe.putLong(address, value);
					Assertions.assertEquals(value, unsafe.getLong(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Float.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final float value = randomFloat(random);
					unsafe.putFloat(address, value);
					Assertions.assertEquals(value, unsafe.getFloat(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Double.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final double value = randomDouble(random);
					unsafe.putDouble(address, value);
					Assertions.assertEquals(value, unsafe.getDouble(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}

		{
			final long address = unsafe.allocateMemory(Long.BYTES);
			try {
				for (int i = 0; i != SAMPLES; ++i) {
					final long value = randomLong(random);
					unsafe.putAddress(address, value);
					Assertions.assertEquals(value, unsafe.getAddress(address));
				}
			} finally {
				unsafe.freeMemory(address);
			}
		}


		{
			long address = unsafe.allocateMemory(1L);
			try {
				address = unsafe.reallocateMemory(address, 2L);
			} finally {
				unsafe.freeMemory(address);
			}
		}


		{
			long address = unsafe.allocateMemory(1L);
			try {
				address = unsafe.reallocateMemory(address, 2L);
			} finally {
				unsafe.freeMemory(address);
			}
		}


		{
			final byte[] array = new byte[1];
			unsafe.setMemory(array, 0, 1L, randomByte(random));
		}

		{
			final long address = unsafe.allocateMemory(1L);
			try {
				unsafe.setMemory(address, 1L, randomByte(random));
			} finally {
				unsafe.freeMemory(address);
			}
		}


		{
			final byte[] src = new byte[1];
			final byte[] dst = new byte[1];
			unsafe.copyMemory(src, 0L, dst, 0L, 1L);
		}

		{
			final long src = unsafe.allocateMemory(1L);
			final long dst = unsafe.allocateMemory(1L);
			try {
				unsafe.copyMemory(src, dst, 1L);
			} finally {
				unsafe.freeMemory(src);
				unsafe.freeMemory(dst);
			}
		}


		Assertions.assertDoesNotThrow(() -> unsafe.staticFieldOffset(TestObject.class.getDeclaredField("STATIC")));
		Assertions.assertDoesNotThrow(() -> unsafe.objectFieldOffset(TestObject.class.getDeclaredField("object")));
		Assertions.assertDoesNotThrow(() -> unsafe.staticFieldBase(TestObject.class.getDeclaredField("STATIC")));


		Assertions.assertDoesNotThrow(() -> unsafe.shouldBeInitialized(Object.class));
		Assertions.assertDoesNotThrow(() -> unsafe.ensureClassInitialized(Object.class));


		Assertions.assertDoesNotThrow(() -> unsafe.arrayBaseOffset(Object[].class));
		Assertions.assertDoesNotThrow(() -> unsafe.arrayIndexScale(Object[].class));


		Assertions.assertDoesNotThrow(unsafe::addressSize);
		Assertions.assertDoesNotThrow(unsafe::pageSize);


		try {
			unsafe.defineClass(null, null, 0, 0, null, null);
		} catch (final Throwable throwable) {
			Assertions.assertFalse(throwable instanceof NoSuchMethodError);
		}
		try {
			unsafe.defineAnonymousClass(null, null, null);
		} catch (final Throwable throwable) {
			Assertions.assertFalse(throwable instanceof NoSuchMethodError);
		}


		Assertions.assertDoesNotThrow(() -> unsafe.allocateInstance(Object.class));

		Assertions.assertThrows(RuntimeException.class, () -> unsafe.throwException(new RuntimeException()));


		{
			final boolean[] array = new boolean[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final boolean value = randomBoolean(random);
				unsafe.putBooleanSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getBooleanSeqCst(array, 0L));
			}
		}

		{
			final char[] array = new char[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final char value = randomChar(random);
				unsafe.putCharSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getCharSeqCst(array, 0L));
			}
		}

		{
			final byte[] array = new byte[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final byte value = randomByte(random);
				unsafe.putByteSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getByteSeqCst(array, 0L));
			}
		}

		{
			final short[] array = new short[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final short value = randomShort(random);
				unsafe.putShortSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getShortSeqCst(array, 0L));
			}
		}

		{
			final int[] array = new int[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final int value = randomInt(random);
				unsafe.putIntSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getIntSeqCst(array, 0L));
			}
		}

		{
			final long[] array = new long[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final long value = randomLong(random);
				unsafe.putLongSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getLongSeqCst(array, 0L));
			}
		}

		{
			final float[] array = new float[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final float value = randomFloat(random);
				unsafe.putFloatSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getFloatSeqCst(array, 0L));
			}
		}

		{
			final double[] array = new double[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final double value = randomDouble(random);
				unsafe.putDoubleSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getDoubleSeqCst(array, 0L));
			}
		}

		{
			final Object[] array = new Object[1];
			for (int i = 0; i != SAMPLES; ++i) {
				final Object value = new Object();
				unsafe.putObjectSeqCst(array, 0L, value);
				Assertions.assertEquals(value, unsafe.getDoubleSeqCst(array, 0L));
			}
		}


		unsafe.loadFence();
		unsafe.storeFence();
		unsafe.fullFence();
	}

	static final class TestObject {
		static final Object STATIC = null;

		final Object object = null;
	}

}
