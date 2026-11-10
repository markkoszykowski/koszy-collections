package com.koszy.util;

import com.koszy.util.buffer.RecordDescriptor;
import com.koszy.util.buffer.RingBufferDescriptor;
import com.koszy.util.buffer.UnsafeBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public final class BufferUtil {

	public static final long BYTE_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_BYTE_BASE_OFFSET;
	public static final long CHAR_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_CHAR_BASE_OFFSET;
	public static final long SHORT_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_SHORT_BASE_OFFSET;
	public static final long INT_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_INT_BASE_OFFSET;
	public static final long LONG_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_LONG_BASE_OFFSET;
	public static final long FLOAT_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_FLOAT_BASE_OFFSET;
	public static final long DOUBLE_ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_DOUBLE_BASE_OFFSET;


	public static final long BYTE_ARRAY_SCALE = OpenUnsafe.ARRAY_BYTE_INDEX_SCALE;
	public static final long CHAR_ARRAY_SCALE = OpenUnsafe.ARRAY_CHAR_INDEX_SCALE;
	public static final long SHORT_ARRAY_SCALE = OpenUnsafe.ARRAY_SHORT_INDEX_SCALE;
	public static final long INT_ARRAY_SCALE = OpenUnsafe.ARRAY_INT_INDEX_SCALE;
	public static final long LONG_ARRAY_SCALE = OpenUnsafe.ARRAY_LONG_INDEX_SCALE;
	public static final long FLOAT_ARRAY_SCALE = OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE;
	public static final long DOUBLE_ARRAY_SCALE = OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE;

	public static final long BYTE_BUFFER_HB_OFFSET = byteBufferHbOffset();
	public static final long CHAR_BUFFER_HB_OFFSET = charBufferHbOffset();
	public static final long SHORT_BUFFER_HB_OFFSET = shortBufferHbOffset();
	public static final long INT_BUFFER_HB_OFFSET = intBufferHbOffset();
	public static final long LONG_BUFFER_HB_OFFSET = longBufferHbOffset();
	public static final long FLOAT_BUFFER_HB_OFFSET = floatBufferHbOffset();
	public static final long DOUBLE_BUFFER_HB_OFFSET = doubleBufferHbOffset();

	public static final long BYTE_BUFFER_OFFSET_OFFSET = byteBufferOffsetOffset();
	public static final long CHAR_BUFFER_OFFSET_OFFSET = charBufferOffsetOffset();
	public static final long SHORT_BUFFER_OFFSET_OFFSET = shortBufferOffsetOffset();
	public static final long INT_BUFFER_OFFSET_OFFSET = intBufferOffsetOffset();
	public static final long LONG_BUFFER_OFFSET_OFFSET = longBufferOffsetOffset();
	public static final long FLOAT_BUFFER_OFFSET_OFFSET = floatBufferOffsetOffset();
	public static final long DOUBLE_BUFFER_OFFSET_OFFSET = doubleBufferOffsetOffset();

	public static final long BUFFER_ADDRESS_OFFSET = bufferAddressOffset();


	private static long byteBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long charBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(CharBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long shortBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(ShortBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long intBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(IntBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long longBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(LongBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long floatBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(FloatBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long doubleBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(FloatBuffer.class.getDeclaredField("hb"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}


	private static long byteBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(ByteBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long charBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(CharBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long shortBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(ShortBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long intBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(IntBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long longBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(LongBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long floatBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(FloatBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}

	private static long doubleBufferOffsetOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(DoubleBuffer.class.getDeclaredField("offset"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}


	private static long bufferAddressOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(Buffer.class.getDeclaredField("address"));
		} catch (final ReflectiveOperationException exception) {
			return LangUtil.rethrow(exception);
		}
	}


	public static byte[] array(final ByteBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (byte[]) unsafe.getObject(buffer, BYTE_BUFFER_HB_OFFSET);
	}

	public static char[] array(final CharBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (char[]) unsafe.getObject(buffer, CHAR_BUFFER_HB_OFFSET);
	}

	public static short[] array(final ShortBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (short[]) unsafe.getObject(buffer, SHORT_BUFFER_HB_OFFSET);
	}

	public static int[] array(final IntBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (int[]) unsafe.getObject(buffer, INT_BUFFER_HB_OFFSET);
	}

	public static long[] array(final LongBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (long[]) unsafe.getObject(buffer, LONG_BUFFER_HB_OFFSET);
	}

	public static float[] array(final FloatBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (float[]) unsafe.getObject(buffer, FLOAT_BUFFER_HB_OFFSET);
	}

	public static double[] array(final DoubleBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return (double[]) unsafe.getObject(buffer, DOUBLE_BUFFER_HB_OFFSET);
	}


	public static int offset(final ByteBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, BYTE_BUFFER_OFFSET_OFFSET);
	}

	public static int offset(final CharBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, CHAR_BUFFER_OFFSET_OFFSET);
	}

	public static int offset(final ShortBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, SHORT_BUFFER_OFFSET_OFFSET);
	}

	public static int offset(final IntBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, INT_BUFFER_OFFSET_OFFSET);
	}

	public static int offset(final LongBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, LONG_BUFFER_OFFSET_OFFSET);
	}

	public static int offset(final FloatBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, FLOAT_BUFFER_OFFSET_OFFSET);
	}

	public static int offset(final DoubleBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, DOUBLE_BUFFER_OFFSET_OFFSET);
	}


	public static long address(final Buffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getLong(buffer, BUFFER_ADDRESS_OFFSET);
	}


	public static ByteBuffer allocateAligned(final int length, final int alignment) {
		Require.isTrue(NumberUtil.isPowerOfTwo(alignment));

		final ByteBuffer buffer = ByteBuffer.allocateDirect(length + alignment);
		final long address = address(buffer);
		final int remainder = (int) (address & (alignment - 1));
		final int offset = alignment - remainder;
		return ((ByteBuffer) buffer.position(offset).limit(offset + length)).slice();
	}

	public static UnsafeBuffer ringBuffer(final int capacity) {
		Require.isNonNegative(capacity);
		return new UnsafeBuffer(allocateAligned(capacity + RingBufferDescriptor.TRAILER_LENGTH, RecordDescriptor.ALIGNMENT));
	}

	private BufferUtil() {
		LangUtil.unreachable();
	}

}
