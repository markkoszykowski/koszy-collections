package com.koszy.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public final class OpenUnsafe {

	private static final OpenUnsafe THE_UNSAFE = new OpenUnsafe();

	public static OpenUnsafe theUnsafe() {
		return THE_UNSAFE;
	}

	public static final int INVALID_FIELD_OFFSET = Unsafe.INVALID_FIELD_OFFSET;

	public static final int ARRAY_BOOLEAN_BASE_OFFSET = Unsafe.ARRAY_BOOLEAN_BASE_OFFSET;
	public static final int ARRAY_BYTE_BASE_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;
	public static final int ARRAY_SHORT_BASE_OFFSET = Unsafe.ARRAY_SHORT_BASE_OFFSET;
	public static final int ARRAY_CHAR_BASE_OFFSET = Unsafe.ARRAY_CHAR_BASE_OFFSET;
	public static final int ARRAY_INT_BASE_OFFSET = Unsafe.ARRAY_INT_BASE_OFFSET;
	public static final int ARRAY_LONG_BASE_OFFSET = Unsafe.ARRAY_LONG_BASE_OFFSET;
	public static final int ARRAY_FLOAT_BASE_OFFSET = Unsafe.ARRAY_FLOAT_BASE_OFFSET;
	public static final int ARRAY_DOUBLE_BASE_OFFSET = Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
	public static final int ARRAY_OBJECT_BASE_OFFSET = Unsafe.ARRAY_OBJECT_BASE_OFFSET;

	public static final int ARRAY_BOOLEAN_INDEX_SCALE = Unsafe.ARRAY_BOOLEAN_INDEX_SCALE;
	public static final int ARRAY_BYTE_INDEX_SCALE = Unsafe.ARRAY_BYTE_INDEX_SCALE;
	public static final int ARRAY_SHORT_INDEX_SCALE = Unsafe.ARRAY_SHORT_INDEX_SCALE;
	public static final int ARRAY_CHAR_INDEX_SCALE = Unsafe.ARRAY_CHAR_INDEX_SCALE;
	public static final int ARRAY_INT_INDEX_SCALE = Unsafe.ARRAY_INT_INDEX_SCALE;
	public static final int ARRAY_LONG_INDEX_SCALE = Unsafe.ARRAY_LONG_INDEX_SCALE;
	public static final int ARRAY_FLOAT_INDEX_SCALE = Unsafe.ARRAY_FLOAT_INDEX_SCALE;
	public static final int ARRAY_DOUBLE_INDEX_SCALE = Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
	public static final int ARRAY_OBJECT_INDEX_SCALE = Unsafe.ARRAY_OBJECT_INDEX_SCALE;

	public static final int ADDRESS_SIZE = Unsafe.ADDRESS_SIZE;

	private static Unsafe getUnsafe() {
		try {
			return ReflectionUtil.get(ReflectionUtil.access(Unsafe.class.getDeclaredField("theUnsafe")));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private final Unsafe theUnsafe = getUnsafe();

	private OpenUnsafe() {
	}


	public boolean unalignedAccess() {
		return false;
	}

	public boolean getBoolean(final Object o, final long offset) {
		return this.theUnsafe.getBoolean(o, offset);
	}

	public void putBoolean(final Object o, final long offset, final boolean x) {
		this.theUnsafe.putBoolean(o, offset, x);
	}

	public char getChar(final Object o, final long offset) {
		return this.theUnsafe.getChar(o, offset);
	}

	public char getCharUnaligned(final Object o, final long offset) {
		if (SystemUtil.BIG_ENDIAN) {
			return (char) NumberUtil.s(this.theUnsafe.getByte(o, offset), this.theUnsafe.getByte(o, offset + 1));
		} else {
			return (char) NumberUtil.s(this.theUnsafe.getByte(o, offset + 1), this.theUnsafe.getByte(o, offset));
		}
	}

	public void putChar(final Object o, final long offset, final char x) {
		this.theUnsafe.putChar(o, offset, x);
	}

	public void putCharUnaligned(final Object o, final long offset, final char x) {
		if (SystemUtil.BIG_ENDIAN) {
			this.theUnsafe.putByte(o, offset, NumberUtil.b0((short) x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b1((short) x));
		} else {
			this.theUnsafe.putByte(o, offset, NumberUtil.b1((short) x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b0((short) x));
		}
	}

	public byte getByte(final Object o, final long offset) {
		return this.theUnsafe.getByte(o, offset);
	}

	public void putByte(final Object o, final long offset, final byte x) {
		this.theUnsafe.putByte(o, offset, x);
	}

	public short getShort(final Object o, final long offset) {
		return this.theUnsafe.getShort(o, offset);
	}

	public short getShortUnaligned(final Object o, final long offset) {
		if (SystemUtil.BIG_ENDIAN) {
			return NumberUtil.s(this.theUnsafe.getByte(o, offset), this.theUnsafe.getByte(o, offset + 1));
		} else {
			return NumberUtil.s(this.theUnsafe.getByte(o, offset + 1), this.theUnsafe.getByte(o, offset));
		}
	}

	public void putShort(final Object o, final long offset, final short x) {
		this.theUnsafe.putShort(o, offset, x);
	}

	public void putShortUnaligned(final Object o, final long offset, final short x) {
		if (SystemUtil.BIG_ENDIAN) {
			this.theUnsafe.putByte(o, offset, NumberUtil.b0(x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b1(x));
		} else {
			this.theUnsafe.putByte(o, offset, NumberUtil.b1(x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b0(x));
		}
	}

	public int getInt(final Object o, final long offset) {
		return this.theUnsafe.getInt(o, offset);
	}

	public int getIntUnaligned(final Object o, final long offset) {
		if (SystemUtil.BIG_ENDIAN) {
			return NumberUtil.i(this.theUnsafe.getByte(o, offset), this.theUnsafe.getByte(o, offset + 1), this.theUnsafe.getByte(o, offset + 2), this.theUnsafe.getByte(o, offset + 3));
		} else {
			return NumberUtil.i(this.theUnsafe.getByte(o, offset + 3), this.theUnsafe.getByte(o, offset + 2), this.theUnsafe.getByte(o, offset + 1), this.theUnsafe.getByte(o, offset));
		}
	}

	public void putInt(final Object o, final long offset, final int x) {
		this.theUnsafe.putInt(o, offset, x);
	}

	public void putIntUnaligned(final Object o, final long offset, final int x) {
		if (SystemUtil.BIG_ENDIAN) {
			this.theUnsafe.putByte(o, offset, NumberUtil.b0(x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b1(x));
			this.theUnsafe.putByte(o, offset + 2, NumberUtil.b2(x));
			this.theUnsafe.putByte(o, offset + 3, NumberUtil.b3(x));
		} else {
			this.theUnsafe.putByte(o, offset, NumberUtil.b3(x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b2(x));
			this.theUnsafe.putByte(o, offset + 2, NumberUtil.b1(x));
			this.theUnsafe.putByte(o, offset + 3, NumberUtil.b0(x));
		}
	}

	public long getLong(final Object o, final long offset) {
		return this.theUnsafe.getLong(o, offset);
	}

	public long getLongUnaligned(final Object o, final long offset) {
		if (SystemUtil.BIG_ENDIAN) {
			return NumberUtil.l(
					this.theUnsafe.getByte(o, offset), this.theUnsafe.getByte(o, offset + 1), this.theUnsafe.getByte(o, offset + 2), this.theUnsafe.getByte(o, offset + 3),
					this.theUnsafe.getByte(o, offset + 4), this.theUnsafe.getByte(o, offset + 5), this.theUnsafe.getByte(o, offset + 6), this.theUnsafe.getByte(o, offset + 7)
			);
		} else {
			return NumberUtil.l(
					this.theUnsafe.getByte(o, offset + 7), this.theUnsafe.getByte(o, offset + 6), this.theUnsafe.getByte(o, offset + 5), this.theUnsafe.getByte(o, offset + 4),
					this.theUnsafe.getByte(o, offset + 3), this.theUnsafe.getByte(o, offset + 2), this.theUnsafe.getByte(o, offset + 1), this.theUnsafe.getByte(o, offset)
			);
		}
	}

	public void putLong(final Object o, final long offset, final long x) {
		this.theUnsafe.putLong(o, offset, x);
	}

	public void putLongUnaligned(final Object o, final long offset, final long x) {
		if (SystemUtil.BIG_ENDIAN) {
			this.theUnsafe.putByte(o, offset, NumberUtil.b0(x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b1(x));
			this.theUnsafe.putByte(o, offset + 2, NumberUtil.b2(x));
			this.theUnsafe.putByte(o, offset + 3, NumberUtil.b3(x));
			this.theUnsafe.putByte(o, offset + 4, NumberUtil.b4(x));
			this.theUnsafe.putByte(o, offset + 5, NumberUtil.b5(x));
			this.theUnsafe.putByte(o, offset + 6, NumberUtil.b6(x));
			this.theUnsafe.putByte(o, offset + 7, NumberUtil.b7(x));
		} else {
			this.theUnsafe.putByte(o, offset, NumberUtil.b7(x));
			this.theUnsafe.putByte(o, offset + 1, NumberUtil.b6(x));
			this.theUnsafe.putByte(o, offset + 2, NumberUtil.b5(x));
			this.theUnsafe.putByte(o, offset + 3, NumberUtil.b4(x));
			this.theUnsafe.putByte(o, offset + 4, NumberUtil.b3(x));
			this.theUnsafe.putByte(o, offset + 5, NumberUtil.b2(x));
			this.theUnsafe.putByte(o, offset + 6, NumberUtil.b1(x));
			this.theUnsafe.putByte(o, offset + 7, NumberUtil.b0(x));
		}
	}

	public float getFloat(final Object o, final long offset) {
		return this.theUnsafe.getFloat(o, offset);
	}

	public void putFloat(final Object o, final long offset, final float x) {
		this.theUnsafe.putFloat(o, offset, x);
	}

	public double getDouble(final Object o, final long offset) {
		return this.theUnsafe.getDouble(o, offset);
	}

	public void putDouble(final Object o, final long offset, final double x) {
		this.theUnsafe.putDouble(o, offset, x);
	}

	public Object getObject(final Object o, final long offset) {
		return this.theUnsafe.getObject(o, offset);
	}

	public void putObject(final Object o, final long offset, final Object x) {
		this.theUnsafe.putObject(o, offset, x);
	}


	public byte getByte(final long address) {
		return this.theUnsafe.getByte(address);
	}

	public void putByte(final long address, final byte x) {
		this.theUnsafe.putByte(address, x);
	}

	public short getShort(final long address) {
		return this.theUnsafe.getShort(address);
	}

	public void putShort(final long address, final short x) {
		this.theUnsafe.putShort(address, x);
	}

	public char getChar(final long address) {
		return this.theUnsafe.getChar(address);
	}

	public void putChar(final long address, final char x) {
		this.theUnsafe.putChar(address, x);
	}

	public int getInt(final long address) {
		return this.theUnsafe.getInt(address);
	}

	public void putInt(final long address, final int x) {
		this.theUnsafe.putInt(address, x);
	}

	public long getLong(final long address) {
		return this.theUnsafe.getLong(address);
	}

	public void putLong(final long address, final long x) {
		this.theUnsafe.putLong(address, x);
	}

	public float getFloat(final long address) {
		return this.theUnsafe.getFloat(address);
	}

	public void putFloat(final long address, final float x) {
		this.theUnsafe.putFloat(address, x);
	}

	public double getDouble(final long address) {
		return this.theUnsafe.getDouble(address);
	}

	public void putDouble(final long address, final double x) {
		this.theUnsafe.putDouble(address, x);
	}

	public long getAddress(final long address) {
		return this.theUnsafe.getAddress(address);
	}

	public void putAddress(final long address, final long x) {
		this.theUnsafe.putAddress(address, x);
	}


	public long allocateMemory(final long bytes) {
		return this.theUnsafe.allocateMemory(bytes);
	}

	public long reallocateMemory(final long address, final long bytes) {
		return this.theUnsafe.reallocateMemory(address, bytes);
	}

	public void setMemory(final Object o, final long offset, final long bytes, final byte value) {
		this.theUnsafe.setMemory(o, offset, bytes, value);
	}

	public void setMemory(final long address, final long bytes, final byte value) {
		this.theUnsafe.setMemory(address, bytes, value);
	}

	public void copyMemory(final Object srcBase, final long srcOffset, final Object destBase, final long destOffset, final long bytes) {
		this.theUnsafe.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
	}

	public void copyMemory(final long srcAddress, final long destAddress, final long bytes) {
		this.theUnsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	public void freeMemory(final long address) {
		this.theUnsafe.freeMemory(address);
	}


	public long staticFieldOffset(final Field f) {
		return this.theUnsafe.staticFieldOffset(f);
	}

	public long objectFieldOffset(final Field f) {
		return this.theUnsafe.objectFieldOffset(f);
	}

	public Object staticFieldBase(final Field f) {
		return this.theUnsafe.staticFieldBase(f);
	}

	public boolean shouldBeInitialized(final Class<?> c) {
		return this.theUnsafe.shouldBeInitialized(c);
	}

	public void ensureClassInitialized(final Class<?> c) {
		this.theUnsafe.ensureClassInitialized(c);
	}

	public int arrayBaseOffset(final Class<?> arrayClass) {
		return this.theUnsafe.arrayBaseOffset(arrayClass);
	}

	public int arrayIndexScale(final Class<?> arrayClass) {
		return this.theUnsafe.arrayIndexScale(arrayClass);
	}


	public int addressSize() {
		return this.theUnsafe.addressSize();
	}

	public int pageSize() {
		return this.theUnsafe.pageSize();
	}


	public Class<?> defineClass(final String name, final byte[] b, final int off, final int len, final ClassLoader loader, final ProtectionDomain protectionDomain) {
		return this.theUnsafe.defineClass(name, b, off, len, loader, protectionDomain);
	}

	public Class<?> defineAnonymousClass(final Class<?> hostClass, final byte[] data, final Object[] cpPatches) {
		return this.theUnsafe.defineAnonymousClass(hostClass, data, cpPatches);
	}

	public Object allocateInstance(final Class<?> cls) throws InstantiationException {
		return this.theUnsafe.allocateInstance(cls);
	}


	public void throwException(final Throwable ee) {
		this.theUnsafe.throwException(ee);
	}


	public boolean getBooleanSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getBooleanVolatile(o, offset);
	}

	public void putBooleanSeqCst(final Object o, final long offset, final boolean x) {
		this.theUnsafe.putBooleanVolatile(o, offset, x);
	}

	public char getCharSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getCharVolatile(o, offset);
	}

	public void putCharSeqCst(final Object o, final long offset, final char x) {
		this.theUnsafe.putCharVolatile(o, offset, x);
	}

	public byte getByteSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getByteVolatile(o, offset);
	}

	public void putByteSeqCst(final Object o, final long offset, final byte x) {
		this.theUnsafe.putByteVolatile(o, offset, x);
	}

	public short getShortSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getShortVolatile(o, offset);
	}

	public void putShortSeqCst(final Object o, final long offset, final short x) {
		this.theUnsafe.putShortVolatile(o, offset, x);
	}

	public int getIntSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getIntVolatile(o, offset);
	}

	public void putIntSeqCst(final Object o, final long offset, final int x) {
		this.theUnsafe.putIntVolatile(o, offset, x);
	}

	public long getLongSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getLongVolatile(o, offset);
	}

	public void putLongSeqCst(final Object o, final long offset, final long x) {
		this.theUnsafe.putLongVolatile(o, offset, x);
	}

	public float getFloatSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getFloatVolatile(o, offset);
	}

	public void putFloatSeqCst(final Object o, final long offset, final float x) {
		this.theUnsafe.putFloatVolatile(o, offset, x);
	}

	public double getDoubleSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getDoubleVolatile(o, offset);
	}

	public void putDoubleSeqCst(final Object o, final long offset, final double x) {
		this.theUnsafe.putDoubleVolatile(o, offset, x);
	}

	public Object getObjectSeqCst(final Object o, final long offset) {
		return this.theUnsafe.getObjectVolatile(o, offset);
	}

	public void putObjectSeqCst(final Object o, final long offset, final Object x) {
		this.theUnsafe.putObjectVolatile(o, offset, x);
	}


	public int getIntAcquire(final Object o, final long offset) {
		return this.theUnsafe.getIntVolatile(o, offset);
	}

	public void putIntRelease(final Object o, final long offset, final int x) {
		this.theUnsafe.putOrderedInt(o, offset, x);
	}

	public long getLongAcquire(final Object o, final long offset) {
		return this.theUnsafe.getLongVolatile(o, offset);
	}

	public void putLongRelease(final Object o, final long offset, final long x) {
		this.theUnsafe.putOrderedLong(o, offset, x);
	}

	public Object getObjectAcquire(final Object o, final long offset) {
		return this.theUnsafe.getObjectVolatile(o, offset);
	}

	public void putObjectRelease(final Object o, final long offset, final Object x) {
		this.theUnsafe.putOrderedObject(o, offset, x);
	}


	public int getIntRelaxed(final Object o, final long offset) {
		return this.theUnsafe.getIntVolatile(o, offset);
	}

	public void putIntRelaxed(final Object o, final long offset, final int x) {
		this.theUnsafe.putOrderedInt(o, offset, x);
	}

	public long getLongRelaxed(final Object o, final long offset) {
		return this.theUnsafe.getLongVolatile(o, offset);
	}

	public void putLongRelaxed(final Object o, final long offset, final long x) {
		this.theUnsafe.putOrderedLong(o, offset, x);
	}

	public Object getObjectRelaxed(final Object o, final long offset) {
		return this.theUnsafe.getObjectVolatile(o, offset);
	}

	public void putObjectRelaxed(final Object o, final long offset, final Object x) {
		this.theUnsafe.putOrderedObject(o, offset, x);
	}


	public void unpark(final Object thread) {
		this.theUnsafe.unpark(thread);
	}

	public void park(final boolean isAbsolute, final long time) {
		this.theUnsafe.park(isAbsolute, time);
	}

	public int getLoadAverage(final double[] loadavg, final int nelems) {
		return this.theUnsafe.getLoadAverage(loadavg, nelems);
	}


	public boolean compareAndSwapInt(final Object o, final long offset, final int expected, final int x) {
		return this.theUnsafe.compareAndSwapInt(o, offset, expected, x);
	}

	public boolean compareAndSwapLong(final Object o, final long offset, final long expected, final long x) {
		return this.theUnsafe.compareAndSwapLong(o, offset, expected, x);
	}

	public boolean compareAndSwapObject(final Object o, final long offset, final Object expected, final Object x) {
		return this.theUnsafe.compareAndSwapObject(o, offset, expected, x);
	}


	public int getAndAddInt(final Object o, final long offset, final int delta) {
		return this.theUnsafe.getAndAddInt(o, offset, delta);
	}

	public long getAndAddLong(final Object o, final long offset, final long delta) {
		return this.theUnsafe.getAndAddLong(o, offset, delta);
	}


	public int getAndSetInt(final Object o, final long offset, final int newValue) {
		return this.theUnsafe.getAndSetInt(o, offset, newValue);
	}

	public long getAndSetLong(final Object o, final long offset, final long newValue) {
		return this.theUnsafe.getAndSetLong(o, offset, newValue);
	}

	public Object getAndSetObject(final Object o, final long offset, final Object newValue) {
		return this.theUnsafe.getAndSetObject(o, offset, newValue);
	}


	public void loadFence() {
		this.theUnsafe.loadFence();
	}

	public void storeFence() {
		this.theUnsafe.storeFence();
	}

	public void fullFence() {
		this.theUnsafe.fullFence();
	}

}
