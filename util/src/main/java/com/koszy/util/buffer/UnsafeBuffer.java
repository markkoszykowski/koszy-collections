package com.koszy.util.buffer;

import com.koszy.util.BufferUtil;
import com.koszy.util.ClassUtil;
import com.koszy.util.NumberUtil;
import com.koszy.util.OpenUnsafe;
import com.koszy.util.Require;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class UnsafeBuffer extends AbstractMutableDirectBuffer implements AtomicBuffer {

	private Buffer buffer;
	private long adjustment;


	public UnsafeBuffer() {
		this.wrap(0L, 0);
	}

	public UnsafeBuffer(final byte[] buffer) {
		this.wrap(buffer);
	}

	public UnsafeBuffer(final byte[] buffer, final int offset, final int length) {
		this.wrap(buffer, offset, length);
	}

	public UnsafeBuffer(final ByteBuffer buffer) {
		this.wrap(buffer);
	}

	public UnsafeBuffer(final ByteBuffer buffer, final int offset, final int length) {
		this.wrap(buffer, offset, length);
	}

	public UnsafeBuffer(final DirectBuffer buffer) {
		this.wrap(buffer);
	}

	public UnsafeBuffer(final DirectBuffer buffer, final int offset, final int length) {
		this.wrap(buffer, offset, length);
	}

	public UnsafeBuffer(final long address, final int length) {
		this.wrap(address, length);
	}


	@Override
	public void wrap(final byte[] array) {
		this.buffer = null;
		this.address = BufferUtil.BYTE_ARRAY_BASE_OFFSET;
		this.array = array;
		this.capacity = BufferUtil.BYTE_ARRAY_SCALE * array.length;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final byte[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = BufferUtil.BYTE_ARRAY_BASE_OFFSET + (BufferUtil.BYTE_ARRAY_SCALE * offset);
		this.array = array;
		this.capacity = BufferUtil.BYTE_ARRAY_SCALE * length;
		this.adjustment = BufferUtil.BYTE_ARRAY_SCALE * offset;
	}

	@Override
	public void wrap(final char[] array) {
		this.buffer = null;
		this.address = BufferUtil.CHAR_ARRAY_BASE_OFFSET;
		this.array = array;
		this.capacity = BufferUtil.CHAR_ARRAY_SCALE * array.length;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final char[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = BufferUtil.CHAR_ARRAY_BASE_OFFSET + (BufferUtil.CHAR_ARRAY_SCALE * offset);
		this.array = array;
		this.capacity = BufferUtil.CHAR_ARRAY_SCALE * length;
		this.adjustment = BufferUtil.CHAR_ARRAY_SCALE * offset;
	}

	@Override
	public void wrap(final short[] array) {
		this.buffer = null;
		this.address = BufferUtil.SHORT_ARRAY_BASE_OFFSET;
		this.array = array;
		this.capacity = BufferUtil.SHORT_ARRAY_SCALE * array.length;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final short[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = BufferUtil.SHORT_ARRAY_BASE_OFFSET + (BufferUtil.SHORT_ARRAY_SCALE * offset);
		this.array = array;
		this.capacity = BufferUtil.SHORT_ARRAY_SCALE * length;
		this.adjustment = BufferUtil.SHORT_ARRAY_SCALE * offset;
	}

	@Override
	public void wrap(final int[] array) {
		this.buffer = null;
		this.address = BufferUtil.INT_ARRAY_BASE_OFFSET;
		this.array = array;
		this.capacity = BufferUtil.INT_ARRAY_SCALE * array.length;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final int[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = BufferUtil.INT_ARRAY_BASE_OFFSET + (BufferUtil.INT_ARRAY_SCALE * offset);
		this.array = array;
		this.capacity = BufferUtil.INT_ARRAY_SCALE * length;
		this.adjustment = BufferUtil.INT_ARRAY_SCALE * offset;
	}

	@Override
	public void wrap(final long[] array) {
		this.buffer = null;
		this.address = BufferUtil.LONG_ARRAY_BASE_OFFSET;
		this.array = array;
		this.capacity = BufferUtil.LONG_ARRAY_SCALE * array.length;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final long[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = BufferUtil.LONG_ARRAY_BASE_OFFSET + (BufferUtil.LONG_ARRAY_SCALE * offset);
		this.array = array;
		this.capacity = BufferUtil.LONG_ARRAY_SCALE * length;
		this.adjustment = BufferUtil.LONG_ARRAY_SCALE * offset;
	}

	@Override
	public void wrap(final float[] array) {
		this.buffer = null;
		this.address = OpenUnsafe.ARRAY_FLOAT_BASE_OFFSET;
		this.array = array;
		this.capacity = (long) array.length * OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final float[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = OpenUnsafe.ARRAY_FLOAT_BASE_OFFSET + ((long) offset * OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE);
		this.array = array;
		this.capacity = (long) length * OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE;
		this.adjustment = (long) offset * OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE;
	}

	@Override
	public void wrap(final double[] array) {
		this.buffer = null;
		this.address = OpenUnsafe.ARRAY_DOUBLE_BASE_OFFSET;
		this.array = array;
		this.capacity = (long) array.length * OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE;
		this.adjustment = 0;
	}

	@Override
	public void wrap(final double[] array, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, array.length);
		}
		this.buffer = null;
		this.address = OpenUnsafe.ARRAY_DOUBLE_BASE_OFFSET + ((long) offset * OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE);
		this.array = array;
		this.capacity = (long) length * OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE;
		this.adjustment = (long) offset * OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE;
	}

	@Override
	public void wrap(final ByteBuffer buffer) {
		this.buffer = buffer;
		this.capacity = buffer.capacity();
		if (buffer.isDirect()) {
			this.address = BufferUtil.address(buffer);
			this.array = null;
			this.adjustment = 0;
		} else {
			this.address = OpenUnsafe.ARRAY_BYTE_BASE_OFFSET + BufferUtil.offset(buffer);
			this.array = BufferUtil.array(buffer);
			this.adjustment = BufferUtil.offset(buffer);
		}
	}

	@Override
	public void wrap(final ByteBuffer buffer, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, buffer.capacity());
		}
		this.buffer = buffer;
		this.capacity = (long) length * OpenUnsafe.ARRAY_BYTE_INDEX_SCALE;
		if (buffer.isDirect()) {
			this.address = BufferUtil.address(buffer) + offset;
			this.array = null;
			this.adjustment = (long) offset * OpenUnsafe.ARRAY_BYTE_INDEX_SCALE;
		} else {
			this.address = OpenUnsafe.ARRAY_BYTE_BASE_OFFSET + (long) (BufferUtil.offset(buffer) + offset) * OpenUnsafe.ARRAY_BYTE_INDEX_SCALE;
			this.array = BufferUtil.array(buffer);
			this.adjustment = BufferUtil.offset(buffer) + offset;
		}
	}

	@Override
	public void wrap(final DirectBuffer buffer) {
		this.buffer = buffer.buffer();
		this.address = buffer.address();
		this.array = buffer.array();
		this.capacity = buffer.capacity();
		this.adjustment = buffer.adjustment();
	}

	@Override
	public void wrap(final DirectBuffer buffer, final int offset, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			Require.checkOffsetLength(offset, length, buffer.capacity());
		}
		this.buffer = buffer.buffer();
		this.address = buffer.address() + offset;
		this.array = buffer.array();
		this.capacity = length;
		this.adjustment = buffer.adjustment() + offset;
	}

	@Override
	public void wrap(final long address, final int length) {
		this.buffer = null;
		this.address = address;
		this.array = null;
		this.capacity = length;
		this.adjustment = 0;
	}


	@Override
	public Buffer buffer() {
		return this.buffer;
	}

	@Override
	public int adjustment() {
		return this.adjustment;
	}


	@Override
	public void check(final int index) {
		Require.checkIndex(index, this.capacity);
	}

	@Override
	public void check(final int offset, final int length) {
		Require.checkOffsetLength(offset, length, this.capacity);
	}


	@Override
	protected void getCheck(final int index, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			this.check(index, length);
		}
	}

	@Override
	protected final void putCheck(final int index, final int length) {
		if (SHOULD_BOUNDS_CHECK) {
			this.check(index, length);
		}
	}

	@Override
	public void verifyAlignment() {
		if (this.array != null) {
			throw new IllegalStateException("AtomicBuffer was created from a byte[] and is not correctly aligned by " + ALIGNMENT);
		} else if ((this.address & (ALIGNMENT - 1L)) != 0L) {
			throw new IllegalStateException("AtomicBuffer is not correctly aligned: address=" + this.address + " is not divisible by " + ALIGNMENT);
		}
	}


	@Override
	public byte getByteSeqCst(final int index) {
		this.getCheck(index, NumberUtil.BYTE_BYTES);
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getByteSeqCst(this.array, this.address + index);
	}

	@Override
	public void putByteSeqCst(final int index, final byte value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.BYTE_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putByteSeqCst(this.array, this.address + index, value);
	}


	@Override
	public char getCharSeqCst(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.CHAR_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getCharSeqCst(this.array, this.address + index);
	}

	@Override
	public void putCharSeqCst(final int index, final char value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.CHAR_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putCharSeqCst(this.array, this.address + index, value);
	}


	@Override
	public short getShortSeqCst(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.SHORT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getShortSeqCst(this.array, this.address + index);
	}

	@Override
	public void putShortSeqCst(final int index, final short value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.SHORT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putShortSeqCst(this.array, this.address + index, value);
	}


	@Override
	public int getIntSeqCst(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getIntSeqCst(this.array, this.address + index);
	}

	@Override
	public void putIntSeqCst(final int index, final int value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putIntSeqCst(this.array, this.address + index, value);
	}


	@Override
	public int getIntAcquire(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getIntAcquire(this.array, this.address + index);
	}

	@Override
	public void putIntRelease(final int index, final int value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putIntRelease(this.array, this.address + index, value);
	}


	@Override
	public int getIntRelaxed(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getIntRelaxed(this.array, this.address + index);
	}

	@Override
	public void putIntRelaxed(final int index, final int value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putIntRelaxed(this.array, this.address + index, value);
	}


	@Override
	public boolean compareAndSetInt(final int index, final int expected, final int value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.compareAndSwapInt(this.array, this.address + index, expected, value);
	}

	@Override
	public int getAndSetInt(final int index, final int value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getAndSetInt(this.array, this.address + index, value);
	}

	@Override
	public int getAndAddInt(final int index, final int delta) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.INT_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getAndAddInt(this.array, this.address + index, delta);
	}


	@Override
	public long getLongSeqCst(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getLongSeqCst(this.array, this.address + index);
	}

	@Override
	public void putLongSeqCst(final int index, final long value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putLongSeqCst(this.array, this.address + index, value);
	}


	@Override
	public long getLongAcquire(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getLongAcquire(this.array, this.address + index);
	}

	@Override
	public void putLongRelease(final int index, final long value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putLongRelease(this.array, this.address + index, value);
	}


	@Override
	public long getLongRelaxed(final int index) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getLongRelaxed(this.array, this.address + index);
	}

	@Override
	public void putLongRelaxed(final int index, final long value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		unsafe.putLongRelaxed(this.array, this.address + index, value);
	}


	@Override
	public boolean compareAndSetLong(final int index, final long expected, final long value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.compareAndSwapLong(this.array, this.address + index, expected, value);
	}

	@Override
	public long getAndSetLong(final int index, final long value) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getAndSetLong(this.array, this.address + index, value);
	}

	@Override
	public long getAndAddLong(final int index, final long delta) {
		if (SHOULD_BOUNDS_CHECK) {
			this.getCheck(index, NumberUtil.LONG_BYTES);
		}
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getAndAddLong(this.array, this.address + index, delta);
	}

	@Override
	public String toString() {
		return "UnsafeBuffer{" +
				"buffer=" + this.buffer +
				", address=0x" + Long.toHexString(this.address) +
				", array=" + (null == this.array ? "null" : (ClassUtil.name(this.array.getClass().getComponentType()) + "[" + Array.getLength(this.array) + "]")) +
				", capacity=" + this.capacity +
				'}';
	}

}
