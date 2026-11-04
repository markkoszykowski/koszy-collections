package com.koszy.logger;

import com.koszy.util.ArrayUtil;
import com.koszy.util.NumberUtil;
import com.koszy.util.Require;
import com.koszy.util.StringUtil;
import com.koszy.util.buffer.MutableDirectBuffer;
import com.koszy.util.buffer.RingBuffer;
import org.slf4j.event.Level;

import java.nio.ByteBuffer;

final class AsyncAppender extends AbstractAppender {

	static final int NULL_LENGTH = -1;

	static int scale(final int size, final float ratio) {
		return (int) (size * (double) ratio);
	}

	static int size(final int level, final byte[] thread, final CharSequence message, final CharSequence throwable, final float ratio) {
		return NumberUtil.INT_BYTES +
				NumberUtil.INT_BYTES + ArrayUtil.length(thread) +
				NumberUtil.INT_BYTES + scale(StringUtil.length(message), ratio) +
				NumberUtil.INT_BYTES + scale(StringUtil.length(throwable), ratio);
	}

	final RingBuffer buffer;

	AsyncAppender(final RingBuffer buffer, final ThreadLocal<Local> local) {
		super(local);
		this.buffer = buffer;
	}

	boolean encode(final Local local, final int level, final byte[] thread, final CharSequence message, final CharSequence throwable, final float ratio) {
		int index;
		local.idle.reset();
		while ((index = this.buffer.claim(1, size(level, thread, message, throwable, ratio))) == RingBuffer.INSUFFICIENT_CAPACITY && ThreadUtil.is) {
			local.idle.idle();
		}

		boolean commit = false;
		try {
			final MutableDirectBuffer buffer = this.buffer.buffer();
			int offset = index;

			buffer.putInt(offset, level);
			offset += NumberUtil.INT_BYTES;

			if (thread == null) {
				buffer.putInt(offset, NULL_LENGTH);
				offset += NumberUtil.INT_BYTES;
			} else {
				buffer.putInt(offset, thread.length);
				offset += NumberUtil.INT_BYTES;

				buffer.putBytes(offset, thread);
				offset += thread.length;
			}

			ByteBuffer bytes = null;

			if (message == null) {
				buffer.putInt(offset, NULL_LENGTH);
				offset += NumberUtil.INT_BYTES;
			} else {
				bytes
			}

			if (throwable == null) {
				buffer.putInt(offset, NULL_LENGTH);
				offset += NumberUtil.INT_BYTES;
			} else {

			}

			commit = true;
		} finally {
			if (commit) {
				this.buffer.commit(index);
			} else {
				this.buffer.abort(index);
			}
		}
		return commit;
	}

	@Override
	void append(final Local local, final Level level, final CharSequence message, final CharSequence throwable) {
		final int value = level.toInt();
		if (!this.encode(local, value, local.thread, message, throwable, local.ratio)) {
			Require.isTrue(this.encode(local, value, local.thread, message, throwable, local.encoder.maxBytesPerChar()));
		}
	}

}
