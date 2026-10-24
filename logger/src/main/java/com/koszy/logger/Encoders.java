package com.koszy.logger;

import com.koszy.util.LangUtil;
import com.koszy.util.NumberUtil;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

final class Encoders {

	static final int MAX_MESSAGE_LENGTH = 1 << 16;

	static final ThreadLocal<Encoders> ENCODERS = ThreadLocal.withInitial(Encoders::new);

	static Encoders get() {
		return ENCODERS.get();
	}

	static void rethrow(final CoderResult result) {
		try {
			if (!result.isUnderflow()) {
				result.throwException();
			}
		} catch (final CharacterCodingException exception) {
			LangUtil.rethrow(exception);
		}
	}

	final CharsetEncoder encoder = Charset.defaultCharset().newEncoder()
			.onMalformedInput(CodingErrorAction.REPLACE)
			.onUnmappableCharacter(CodingErrorAction.REPLACE);
	final Set<Object> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
	final StringBuilder builder = new StringBuilder();
	final Object[] args = new Object[2];
	final byte[] thread = Thread.currentThread().getName().getBytes();

	ByteBuffer buffer = null;

	byte[] thread() {
		return this.thread;
	}

	/// @see String#getBytes(Charset)
	ByteBuffer buffer(final CharSequence string) {
		final int estimate = (int) (string.length() * (double) this.encoder.maxBytesPerChar());
		final int capacity = Math.min(NumberUtil.nextPowerOfTwo(estimate), MAX_MESSAGE_LENGTH);
		if (this.buffer == null || this.builder.capacity() < capacity) {
			return (ByteBuffer) (this.buffer = ByteBuffer.allocate(capacity)).clear();
		} else {
			return (ByteBuffer) this.buffer.clear();
		}
	}

	ByteBuffer format(final CharSequence string) {
		try {
			final CharBuffer chars = CharBuffer.wrap(string);
			final ByteBuffer buffer = this.buffer(chars);
			rethrow(this.encoder.encode(chars, buffer, true));
			rethrow(this.encoder.flush(buffer));
			return (ByteBuffer) buffer.flip();
		} finally {
			this.encoder.reset();
		}
	}

	ByteBuffer format(final String format) {
		return this.format((CharSequence) format);
	}

	ByteBuffer format(final String format, final Object arg) {
		try {
			this.args[0] = arg;
			return this.format(LoggerUtil.format(format, this.args, 1, this.dejaVu, this.builder));
		} finally {
			this.builder.setLength(0);
			Arrays.fill(this.args, null);
		}
	}

	ByteBuffer format(final String format, final Object arg1, final Object arg2) {
		try {
			this.args[0] = arg1;
			this.args[1] = arg2;
			return this.format(LoggerUtil.format(format, this.args, 2, this.dejaVu, this.builder));
		} finally {
			this.builder.setLength(0);
			Arrays.fill(this.args, null);
		}
	}

	ByteBuffer format(final String format, final Object[] args, final int length) {
		try {
			return this.format(LoggerUtil.format(format, args, length, this.dejaVu, this.builder));
		} finally {
			this.builder.setLength(0);
		}
	}

	void release(final ByteBuffer buffer) {
		assert buffer == this.buffer;
		buffer.clear();
	}

}
