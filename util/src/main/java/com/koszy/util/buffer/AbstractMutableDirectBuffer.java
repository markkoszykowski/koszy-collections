package com.koszy.util.buffer;

import com.koszy.util.BufferUtil;
import com.koszy.util.NumberUtil;
import com.koszy.util.OpenUnsafe;
import com.koszy.util.Require;
import com.koszy.util.SystemUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public abstract class AbstractMutableDirectBuffer implements MutableDirectBuffer {

	protected long address;
	protected Object array;
	protected long capacity;

	protected AbstractMutableDirectBuffer() {
	}

	@Override
	public long address() {
		return this.address;
	}

	@Override
	public Object array() {
		return this.array;
	}

	@Override
	public long capacity() {
		return this.capacity;
	}


	protected abstract void getCheck(int index, int length);

	protected abstract void putCheck(int index, int length);

	@Override
	public void setMemory(final int index, final int length, final byte value) {
		this.putCheck(index, length);

		final Object array = this.array;
		final long offset = this.address + index;

		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (length < (1 << 8)) {
			final long bits = NumberUtil.l(value, value, value, value, value, value, value, value);

			int i = 0;
			for (final int end = (length & -NumberUtil.LONG_BYTES); i < end; i += NumberUtil.LONG_BYTES) {
				unsafe.putLong(array, offset + i, bits);
			}
			for (; i < length; i++) {
				unsafe.putByte(array, offset + i, value);
			}
		} else {
			unsafe.setMemory(array, offset, length, value);
		}
	}


	@Override
	public byte getByte(final int index) {
		this.getCheck(index, NumberUtil.BYTE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getByte(this.array, this.address + index);
	}

	@Override
	public void putByte(final int index, final byte value) {
		this.putCheck(index, NumberUtil.BYTE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putByte(this.array, this.address + index, value);
	}


	@Override
	public char getChar(final int index) {
		this.getCheck(index, NumberUtil.CHAR_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getChar(this.array, this.address + index);
	}

	@Override
	public void putChar(final int index, final char value) {
		this.putCheck(index, NumberUtil.CHAR_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putChar(this.array, this.address + index, value);
	}

	@Override
	public char getCharUnaligned(final int index) {
		this.getCheck(index, NumberUtil.CHAR_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			return unsafe.getChar(this.array, this.address + index);
		} else {
			return unsafe.getCharUnaligned(this.array, this.address + index);
		}
	}

	@Override
	public void putCharUnaligned(final int index, final char value) {
		this.putCheck(index, NumberUtil.CHAR_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putChar(this.array, this.address + index, value);
		} else {
			unsafe.putCharUnaligned(this.array, this.address + index, value);
		}
	}

	@Override
	public char getChar(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.CHAR_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final char bits = unsafe.getChar(this.array, this.address + index);
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : (char) Short.reverseBytes((short) bits);
	}

	@Override
	public void putChar(final int index, final char value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.CHAR_BYTES);
		final char bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : (char) Short.reverseBytes((short) value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putChar(this.array, this.address + index, bits);
	}

	@Override
	public char getCharUnaligned(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.CHAR_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final char bits;
		if (SystemUtil.UNALIGNED_ACCESS) {
			bits = unsafe.getChar(this.array, this.address + index);
		} else {
			bits = unsafe.getCharUnaligned(this.array, this.address + index);
		}
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : (char) Short.reverseBytes((short) bits);
	}

	@Override
	public void putCharUnaligned(final int index, final char value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.CHAR_BYTES);
		final char bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : (char) Short.reverseBytes((short) value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putChar(this.array, this.address + index, bits);
		} else {
			unsafe.putCharUnaligned(this.array, this.address + index, bits);
		}
	}


	@Override
	public short getShort(final int index) {
		this.getCheck(index, NumberUtil.SHORT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getShort(this.array, this.address + index);
	}

	@Override
	public void putShort(final int index, final short value) {
		this.putCheck(index, NumberUtil.SHORT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putShort(this.array, this.address + index, value);
	}

	@Override
	public short getShortUnaligned(final int index) {
		this.getCheck(index, NumberUtil.SHORT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			return unsafe.getShort(this.array, this.address + index);
		} else {
			return unsafe.getShortUnaligned(this.array, this.address + index);
		}
	}

	@Override
	public void putShortUnaligned(final int index, final short value) {
		this.putCheck(index, NumberUtil.SHORT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putShort(this.array, this.address + index, value);
		} else {
			unsafe.putShortUnaligned(this.array, this.address + index, value);
		}
	}

	@Override
	public short getShort(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.SHORT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final short bits = unsafe.getShort(this.array, this.address + index);
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : Short.reverseBytes(bits);
	}

	@Override
	public void putShort(final int index, final short value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.SHORT_BYTES);
		final short bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : Short.reverseBytes(value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putShort(this.array, this.address + index, bits);
	}

	@Override
	public short getShortUnaligned(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.SHORT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final short bits;
		if (SystemUtil.UNALIGNED_ACCESS) {
			bits = unsafe.getShort(this.array, this.address + index);
		} else {
			bits = unsafe.getShortUnaligned(this.array, this.address + index);
		}
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : Short.reverseBytes(bits);
	}

	@Override
	public void putShortUnaligned(final int index, final short value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.SHORT_BYTES);
		final short bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : Short.reverseBytes(value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putShort(this.array, this.address + index, bits);
		} else {
			unsafe.putShortUnaligned(this.array, this.address + index, bits);
		}
	}


	@Override
	public int getInt(final int index) {
		this.getCheck(index, NumberUtil.INT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(this.array, this.address + index);
	}

	@Override
	public void putInt(final int index, final int value) {
		this.putCheck(index, NumberUtil.INT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putInt(this.array, this.address + index, value);
	}

	@Override
	public int getIntUnaligned(final int index) {
		this.getCheck(index, NumberUtil.INT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			return unsafe.getInt(this.array, this.address + index);
		} else {
			return unsafe.getIntUnaligned(this.array, this.address + index);
		}
	}

	@Override
	public void putIntUnaligned(final int index, final int value) {
		this.putCheck(index, NumberUtil.INT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putInt(this.array, this.address + index, value);
		} else {
			unsafe.putIntUnaligned(this.array, this.address + index, value);
		}
	}

	@Override
	public int getInt(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.INT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final int bits = unsafe.getInt(this.array, this.address + index);
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : Integer.reverseBytes(bits);
	}

	@Override
	public void putInt(final int index, final int value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.INT_BYTES);
		final int bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : Integer.reverseBytes(value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putInt(this.array, this.address + index, bits);
	}

	@Override
	public int getIntUnaligned(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.INT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final int bits;
		if (SystemUtil.UNALIGNED_ACCESS) {
			bits = unsafe.getInt(this.array, this.address + index);
		} else {
			bits = unsafe.getIntUnaligned(this.array, this.address + index);
		}
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : Integer.reverseBytes(bits);
	}

	@Override
	public void putIntUnaligned(final int index, final int value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.INT_BYTES);
		final int bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : Integer.reverseBytes(value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putInt(this.array, this.address + index, bits);
		} else {
			unsafe.putIntUnaligned(this.array, this.address + index, bits);
		}
	}


	@Override
	public long getLong(final int index) {
		this.getCheck(index, NumberUtil.LONG_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getLong(this.array, this.address + index);
	}

	@Override
	public void putLong(final int index, final long value) {
		this.putCheck(index, NumberUtil.LONG_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putLong(this.array, this.address + index, value);
	}

	@Override
	public long getLongUnaligned(final int index) {
		this.getCheck(index, NumberUtil.LONG_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			return unsafe.getLong(this.array, this.address + index);
		} else {
			return unsafe.getLongUnaligned(this.array, this.address + index);
		}
	}

	@Override
	public void putLongUnaligned(final int index, final long value) {
		this.putCheck(index, NumberUtil.LONG_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putLong(this.array, this.address + index, value);
		} else {
			unsafe.putLongUnaligned(this.array, this.address + index, value);
		}
	}

	@Override
	public long getLong(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.LONG_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final long bits = unsafe.getLong(this.array, this.address + index);
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : Long.reverseBytes(bits);
	}

	@Override
	public void putLong(final int index, final long value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.LONG_BYTES);
		final long bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : Long.reverseBytes(value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putLong(this.array, this.address + index, bits);
	}

	@Override
	public long getLongUnaligned(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.LONG_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final long bits;
		if (SystemUtil.UNALIGNED_ACCESS) {
			bits = unsafe.getLong(this.array, this.address + index);
		} else {
			bits = unsafe.getLongUnaligned(this.array, this.address + index);
		}
		return byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? bits : Long.reverseBytes(bits);
	}

	@Override
	public void putLongUnaligned(final int index, final long value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.LONG_BYTES);
		final long bits = byteOrder == SystemUtil.NATIVE_BYTE_ORDER ? value : Long.reverseBytes(value);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (SystemUtil.UNALIGNED_ACCESS) {
			unsafe.putLong(this.array, this.address + index, bits);
		} else {
			unsafe.putLongUnaligned(this.array, this.address + index, bits);
		}
	}


	@Override
	public float getFloat(final int index) {
		this.getCheck(index, NumberUtil.FLOAT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getFloat(this.array, this.address + index);
	}

	@Override
	public void putFloat(final int index, final float value) {
		this.putCheck(index, NumberUtil.FLOAT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putFloat(this.array, this.address + index, value);
	}

	@Override
	public float getFloat(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.FLOAT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (byteOrder != SystemUtil.NATIVE_BYTE_ORDER) {
			final int bits = unsafe.getInt(this.array, this.address + index);
			return Float.intBitsToFloat(Integer.reverseBytes(bits));
		} else {
			return unsafe.getFloat(this.array, this.address + index);
		}
	}

	@Override
	public void putFloat(final int index, final float value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.FLOAT_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (byteOrder != SystemUtil.NATIVE_BYTE_ORDER) {
			final int bits = Integer.reverseBytes(Float.floatToRawIntBits(value));
			unsafe.putInt(this.array, this.address + index, bits);
		} else {
			unsafe.putFloat(this.array, this.address + index, value);
		}
	}


	@Override
	public double getDouble(final int index) {
		this.getCheck(index, NumberUtil.DOUBLE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getDouble(this.array, this.address + index);
	}

	@Override
	public void putDouble(final int index, final double value) {
		this.putCheck(index, NumberUtil.DOUBLE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putDouble(this.array, this.address + index, value);
	}

	@Override
	public double getDouble(final int index, final ByteOrder byteOrder) {
		this.getCheck(index, NumberUtil.DOUBLE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (byteOrder != SystemUtil.NATIVE_BYTE_ORDER) {
			final long bits = unsafe.getLong(this.array, this.address + index);
			return Double.longBitsToDouble(Long.reverseBytes(bits));
		} else {
			return unsafe.getDouble(this.array, this.address + index);
		}
	}

	@Override
	public void putDouble(final int index, final double value, final ByteOrder byteOrder) {
		this.putCheck(index, NumberUtil.DOUBLE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		if (byteOrder != SystemUtil.NATIVE_BYTE_ORDER) {
			final long bits = Long.reverseBytes(Double.doubleToRawLongBits(value));
			unsafe.putLong(this.array, this.address + index, bits);
		} else {
			unsafe.putDouble(this.array, this.address + index, value);
		}
	}


	@Override
	public void getBytes(final int index, final byte[] array) {
		this.getCheck(index, array.length);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(this.array, this.address + index, array, BufferUtil.ARRAY_BASE_OFFSET, array.length);
	}

	@Override
	public void putBytes(final int index, final byte[] array) {
		this.putCheck(index, array.length);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(array, BufferUtil.ARRAY_BASE_OFFSET, this.array, this.address + index, array.length);
	}


	@Override
	public void getBytes(final int index, final byte[] array, final int offset, final int length) {
		this.getCheck(index, length);
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(this.array, this.address + index, array, BufferUtil.ARRAY_BASE_OFFSET + offset, length);
	}

	@Override
	public void putBytes(final int index, final byte[] array, final int offset, final int length) {
		this.putCheck(index, length);
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(array, BufferUtil.ARRAY_BASE_OFFSET + offset, this.array, this.address + index, length);
	}


	@Override
	public void getBytes(final int index, final ByteBuffer buffer, final int length) {
		final int offset = buffer.position();
		this.getBytes(index, buffer, offset, length);
		buffer.position(offset + length);
	}

	@Override
	public void putBytes(final int index, final ByteBuffer buffer, final int length) {
		final int offset = buffer.position();
		this.putBytes(index, buffer, offset, length);
		buffer.position(offset + length);
	}


	@Override
	public void getBytes(final int index, final ByteBuffer buffer, final int offset, final int length) {
		this.getCheck(index, length);
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, buffer.capacity());
		}
		final byte[] array;
		final long address;
		if (buffer.isDirect()) {
			array = null;
			address = BufferUtil.address(buffer);
		} else {
			array = BufferUtil.array(buffer);
			address = BufferUtil.ARRAY_BASE_OFFSET + BufferUtil.offset(buffer);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(this.array, this.address + index, array, address + offset, length);
	}

	@Override
	public void putBytes(final int index, final ByteBuffer buffer, final int offset, final int length) {
		this.putCheck(index, length);
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, buffer.capacity());
		}
		final byte[] array;
		final long address;
		if (buffer.isDirect()) {
			array = null;
			address = BufferUtil.address(buffer);
		} else {
			array = BufferUtil.array(buffer);
			address = BufferUtil.ARRAY_BASE_OFFSET + BufferUtil.offset(buffer);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(array, address + offset, this.array, this.address + index, length);
	}


	@Override
	public void getBytes(final int index, final MutableDirectBuffer buffer, final int offset, final int length) {
		this.getCheck(index, length);
		if (SHOULD_BOUNDS_CHECK) {
			buffer.check(offset, length);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(this.array, this.address + index, buffer.array(), buffer.address() + offset, length);
	}

	@Override
	public void putBytes(final int index, final DirectBuffer buffer, final int offset, final int length) {
		this.putCheck(index, length);
		if (SHOULD_BOUNDS_CHECK) {
			buffer.check(offset, length);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.copyMemory(buffer.array(), buffer.address() + offset, this.array, this.address + index, length);
	}


	@Override
	public int hashCode() {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();

		final long address = this.address;
		final Object array = this.array;
		final int capacity = this.capacity;

		int hash = 0;

		int i = 0;
		for (final int end = capacity & -NumberUtil.LONG_BYTES; i < end; i += NumberUtil.LONG_BYTES) {
			hash = 31 * hash + Long.hashCode(unsafe.getLong(array, address + i));
		}
		for (; i < capacity; i++) {
			hash = 31 * hash + unsafe.getByte(array, address + i);
		}
		return hash;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || Objects.equals(this.getClass(), object.getClass())) {
			return false;
		}

		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		final AbstractMutableDirectBuffer that = (AbstractMutableDirectBuffer) object;

		final int length = this.capacity;
		if (that.capacity != length) {
			return false;
		}

		final long thisAddress = this.address;
		final long thatAddress = that.address;
		final Object thisArray = this.array;
		final Object thatArray = that.array;

		int i = 0;
		for (final int end = length & -NumberUtil.LONG_BYTES; i < end; i += NumberUtil.LONG_BYTES) {
			if (unsafe.getLong(thisArray, thisAddress + i) != unsafe.getLong(thatArray, thatAddress + i)) {
				return false;
			}
		}
		for (; i < length; i++) {
			if (unsafe.getByte(thisArray, thisAddress + i) != unsafe.getByte(thatArray, thatAddress + i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(final DirectBuffer that) {
		if (this == that) {
			return 0;
		}

		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();

		final long thisAddress = this.address;
		final long thatAddress = that.address();
		final Object thisArray = this.array;
		final Object thatArray = that.array();
		final int thisCapacity = this.capacity;
		final int thatCapacity = that.capacity();

		final int capacity = Math.min(thisCapacity, thatCapacity);

		int i = 0;
		for (final int end = capacity & -NumberUtil.LONG_BYTES; i < end; i += NumberUtil.LONG_BYTES) {
			if (unsafe.getLong(thisArray, thisAddress + i) != unsafe.getLong(thatArray, thatAddress + i)) {
				break;
			}
		}
		for (; i < capacity; i++) {
			final int compare = Byte.compare(unsafe.getByte(thisArray, thisAddress + i), unsafe.getByte(thatArray, thatAddress + i));
			if (compare != 0) {
				return compare;
			}
		}
		return Integer.compare(thisCapacity, thatCapacity);
	}

}
