package com.koszy.util.buffer;

import com.koszy.util.LangUtil;
import com.koszy.util.NumberUtil;

public final class RingBufferDescriptor {

	public static final int HEAD_CACHE_POSITION_OFFSET;

	public static final int HEAD_POSITION_OFFSET;

	public static final int TAIL_POSITION_OFFSET;

	public static final int TRAILER_LENGTH;

	static {
		int offset = 0;

		offset += (NumberUtil.CACHE_LINE_LENGTH * 2);
		HEAD_CACHE_POSITION_OFFSET = offset;

		offset += (NumberUtil.CACHE_LINE_LENGTH * 2);
		HEAD_POSITION_OFFSET = offset;

		offset += (NumberUtil.CACHE_LINE_LENGTH * 2);
		TAIL_POSITION_OFFSET = offset;

		offset += (NumberUtil.CACHE_LINE_LENGTH * 2);
		TRAILER_LENGTH = offset;
	}

	public static int checkCapacity(final int capacity, final int min) {
		final int size = capacity - TRAILER_LENGTH;
		if (!NumberUtil.isPowerOfTwo(size)) {
			throw new IllegalArgumentException("capacity must be a positive power of 2 + TRAILER_LENGTH: capacity=" + capacity);
		}
		if (size < min) {
			throw new IllegalArgumentException("insufficient capacity: min=" + (min + TRAILER_LENGTH) + ", capacity=" + capacity);
		}
		return size;
	}

	private RingBufferDescriptor() {
		LangUtil.unreachable();
	}

}
