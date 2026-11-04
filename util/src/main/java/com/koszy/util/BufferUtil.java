package com.koszy.util;

import com.koszy.util.buffer.RecordDescriptor;
import com.koszy.util.buffer.RingBufferDescriptor;
import com.koszy.util.buffer.UnsafeBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BufferUtil {

	public static final long ARRAY_BASE_OFFSET = OpenUnsafe.ARRAY_BYTE_BASE_OFFSET;

	public static final long BYTE_BUFFER_HB_OFFSET = byteBufferHbOffset();

	public static final long BYTE_BUFFER_OFFSET_OFFSET = byteBufferOffsetOffset();

	public static final long BYTE_BUFFER_ADDRESS_OFFSET = bufferAddressOffset();

	private static long byteBufferHbOffset() {
		try {
			final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
			return unsafe.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
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

	public static int offset(final ByteBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getInt(buffer, BYTE_BUFFER_OFFSET_OFFSET);
	}

	public static long address(final ByteBuffer buffer) {
		final OpenUnsafe unsafe = OpenUnsafe.theUnsafe();
		return unsafe.getLong(buffer, BYTE_BUFFER_ADDRESS_OFFSET);
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
