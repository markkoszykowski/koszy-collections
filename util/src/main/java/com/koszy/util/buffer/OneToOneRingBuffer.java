package com.koszy.util.buffer;

import com.koszy.util.NumberUtil;

public final class OneToOneRingBuffer implements RingBuffer {

	public static final int MIN_CAPACITY = RecordDescriptor.HEADER_LENGTH * 2;

	private final AtomicBuffer buffer;
	private final int capacity;
	private final int max;

	private final int headCachePositionIndex;
	private final int headPositionIndex;
	private final int tailPositionIndex;

	public OneToOneRingBuffer(final AtomicBuffer buffer) {
		this.capacity = RingBufferDescriptor.checkCapacity(buffer.capacity(), MIN_CAPACITY);

		buffer.verifyAlignment();

		this.buffer = buffer;
		this.max = MIN_CAPACITY == this.capacity ? 0 : Math.max(RecordDescriptor.HEADER_LENGTH, this.capacity >> 3);
		this.headCachePositionIndex = this.capacity + RingBufferDescriptor.HEAD_CACHE_POSITION_OFFSET;
		this.headPositionIndex = this.capacity + RingBufferDescriptor.HEAD_POSITION_OFFSET;
		this.tailPositionIndex = this.capacity + RingBufferDescriptor.TAIL_POSITION_OFFSET;
	}

	@Override
	public AtomicBuffer buffer() {
		return this.buffer;
	}

	@Override
	public int capacity() {
		return this.capacity;
	}

	@Override
	public int maxMessageLength() {
		return this.max;
	}

	@Override
	public long producerPosition() {
		return this.buffer.getLongSeqCst(this.tailPositionIndex);
	}

	@Override
	public long consumerPosition() {
		return this.buffer.getLongSeqCst(this.headPositionIndex);
	}

	@Override
	public int size() {
		long tail;
		long headBefore;
		long headAfter = this.buffer.getLongSeqCst(this.headPositionIndex);

		do {
			headBefore = headAfter;
			tail = this.buffer.getLongSeqCst(this.tailPositionIndex);
			headAfter = this.buffer.getLongSeqCst(this.headPositionIndex);
		}
		while (headAfter != headBefore);

		final long size = tail - headAfter;
		if (size < 0L) {
			return 0;
		} else if (this.capacity < size) {
			return this.capacity;
		} else {
			return (int) size;
		}
	}

	private void check(final int length) {
		if (length < 0) {
			throw new IllegalArgumentException("invalid message length=" + length);
		} else if (this.max < length) {
			throw new IllegalArgumentException("encoded message exceeds maxMsgLength=" + this.max + ", length=" + length);
		}
	}

	private int claim(final int length) {
		final int aligned = NumberUtil.align(length, RecordDescriptor.ALIGNMENT);
		final int required = RecordDescriptor.HEADER_LENGTH + aligned;
		final int mask = this.capacity - 1;

		long head = this.buffer.getLong(this.headCachePositionIndex);
		final long tail = this.buffer.getLong(this.tailPositionIndex);
		final int available = this.capacity - (int) (tail - head);

		if (available < required) {
			head = this.buffer.getLongAcquire(this.headPositionIndex);
			if ((this.capacity - (int) (tail - head)) < required) {
				return INSUFFICIENT_CAPACITY;
			}
			this.buffer.putLong(this.headCachePositionIndex, head);
		}

		final int index = (int) tail & mask;
		final int remaining = this.capacity - index;

		int padding = 0;
		int write = index;
		long next = tail + aligned;

		if (aligned == remaining) {
			this.buffer.putLongRelease(this.tailPositionIndex, next);
			this.buffer.putLong(0, 0L);
			return index;
		}

		if (remaining < required) {
			write = 0;
			int read = (int) head & mask;
			if (read < required) {
				head = this.buffer.getLongAcquire(this.headPositionIndex);
				read = (int) head & mask;
				if (read < required) {
					write = INSUFFICIENT_CAPACITY;
					next = tail;
				}
				this.buffer.putLong(this.headCachePositionIndex, head);
			}

			padding = remaining;
			next += padding;
		}

		this.buffer.putLongRelease(this.tailPositionIndex, next);

		if (padding != 0) {
			this.buffer.putLong(0, 0L);

			this.buffer.putIntRelease(RecordDescriptor.lengthOffset(index), -padding);
			this.buffer.putInt(RecordDescriptor.typeOffset(index), PADDING_TYPE);
			this.buffer.putIntRelease(RecordDescriptor.lengthOffset(index), padding);
		}

		if (write != INSUFFICIENT_CAPACITY) {
			this.buffer.putLong(write + aligned, 0L);
		}

		return write;
	}

	@Override
	public boolean write(final int type, final DirectBuffer srcBuffer, final int offset, final int length) {
		RecordDescriptor.checkType(type);
		this.check(length);

		final int recordLength = RecordDescriptor.HEADER_LENGTH + length;
		final int recordIndex = this.claim(recordLength);

		if (INSUFFICIENT_CAPACITY == recordIndex) {
			return false;
		}

		this.buffer.putIntRelease(RecordDescriptor.lengthOffset(recordIndex), -recordLength);
		this.buffer.putInt(RecordDescriptor.typeOffset(recordIndex), type);
		this.buffer.putBytes(RecordDescriptor.messageOffset(recordIndex), srcBuffer, offset, length);
		this.buffer.putIntRelease(RecordDescriptor.lengthOffset(recordIndex), recordLength);

		return true;
	}

	@Override
	public int claim(final int type, final int length) {
		RecordDescriptor.checkType(type);
		this.check(length);

		final int recordLength = RecordDescriptor.HEADER_LENGTH + length;
		final int recordIndex = this.claim(recordLength);

		if (INSUFFICIENT_CAPACITY == recordIndex) {
			return recordIndex;
		}

		this.buffer.putIntRelease(RecordDescriptor.lengthOffset(recordIndex), -recordLength);
		this.buffer.putInt(RecordDescriptor.typeOffset(recordIndex), type);
		return RecordDescriptor.messageOffset(recordIndex);
	}

	private int computeRecordIndex(final int index) {
		final int recordIndex = index - RecordDescriptor.HEADER_LENGTH;
		if (recordIndex < 0 || (this.capacity - RecordDescriptor.HEADER_LENGTH) < recordIndex) {
			throw new IllegalArgumentException("invalid message index " + index);
		}
		return recordIndex;
	}

	private int verifyClaimedSpaceNotReleased(final AtomicBuffer buffer, final int recordIndex) {
		final int recordLength = buffer.getInt(RecordDescriptor.lengthOffset(recordIndex));
		if (0 <= recordLength) {
			throw new IllegalStateException("claimed space previously " + (PADDING_TYPE == buffer.getInt(RecordDescriptor.typeOffset(recordIndex)) ? "aborted" : "committed"));
		}
		return recordLength;
	}

	@Override
	public void commit(final int index) {
		final int recordIndex = this.computeRecordIndex(index);
		final int recordLength = this.verifyClaimedSpaceNotReleased(this.buffer, recordIndex);
		this.buffer.putIntRelease(RecordDescriptor.lengthOffset(recordIndex), -recordLength);
	}

	@Override
	public void abort(final int index) {
		final int recordIndex = this.computeRecordIndex(index);
		final int recordLength = this.verifyClaimedSpaceNotReleased(this.buffer, recordIndex);
		this.buffer.putInt(RecordDescriptor.typeOffset(recordIndex), PADDING_TYPE);
		this.buffer.putIntRelease(RecordDescriptor.lengthOffset(recordIndex), -recordLength);
	}

	@Override
	public int read(final MessageHandler handler) {
		return this.read(handler, Integer.MAX_VALUE);
	}

	@Override
	public int read(final MessageHandler handler, final int limit) {
		final long head = this.buffer.getLong(this.headPositionIndex);

		int records = 0;
		int bytes = 0;

		final int read = (int) head & (this.capacity - 1);
		final int contiguous = this.capacity - read;

		try {
			while ((bytes < contiguous) && (records < limit)) {
				final int recordIndex = read + bytes;
				final int recordLength = this.buffer.getIntAcquire(RecordDescriptor.lengthOffset(recordIndex));
				if (recordLength <= 0) {
					break;
				}

				bytes += NumberUtil.align(recordLength, RecordDescriptor.ALIGNMENT);

				final int type = this.buffer.getInt(RecordDescriptor.typeOffset(recordIndex));
				if (type == PADDING_TYPE) {
					continue;
				}

				handler.handle(type, this.buffer, recordIndex + RecordDescriptor.HEADER_LENGTH, recordLength - RecordDescriptor.HEADER_LENGTH);
				++records;
			}
		} finally {
			if (0 < bytes) {
				this.buffer.putLongRelease(this.headPositionIndex, head + bytes);
			}
		}

		return records;
	}

	@Override
	public int read(final ControlledMessageHandler handler) {
		return this.read(handler, Integer.MAX_VALUE);
	}

	@Override
	public int read(final ControlledMessageHandler handler, final int limit) {
		long head = this.buffer.getLong(this.headPositionIndex);

		int records = 0;
		int bytes = 0;

		int read = (int) head & (this.capacity - 1);
		final int contiguous = this.capacity - read;

		try {
			while ((bytes < contiguous) && (records < limit)) {
				final int recordIndex = read + bytes;
				final int recordLength = this.buffer.getIntAcquire(RecordDescriptor.lengthOffset(recordIndex));
				if (recordLength <= 0) {
					break;
				}

				final int aligned = NumberUtil.align(recordLength, RecordDescriptor.ALIGNMENT);
				bytes += aligned;

				final int type = this.buffer.getInt(RecordDescriptor.typeOffset(recordIndex));
				if (type == PADDING_TYPE) {
					continue;
				}

				final ControlledMessageHandler.Action action = handler.handle(type, this.buffer, recordIndex + RecordDescriptor.HEADER_LENGTH, recordLength - RecordDescriptor.HEADER_LENGTH);
				if (action == ControlledMessageHandler.Action.ABORT) {
					bytes -= aligned;
					break;
				}

				++records;

				if (action == ControlledMessageHandler.Action.BREAK) {
					break;
				}
				if (action == ControlledMessageHandler.Action.COMMIT) {
					this.buffer.putLongRelease(this.headPositionIndex, head + bytes);
					read += bytes;
					head += bytes;
					bytes = 0;
				}
			}
		} finally {
			if (0 < bytes) {
				this.buffer.putLongRelease(this.headPositionIndex, head + bytes);
			}
		}

		return records;
	}

}
