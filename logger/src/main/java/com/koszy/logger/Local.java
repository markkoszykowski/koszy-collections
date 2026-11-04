package com.koszy.logger;

import com.koszy.util.Flyweight;
import com.koszy.util.idle.IdleStrategy;
import org.slf4j.event.Level;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

final class Local extends Flyweight<Local> {

	final Set<Object> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
	final StringBuilder message = new StringBuilder();
	final StringBuilder throwable = new StringBuilder();
	final Object[] args = new Object[2];

	final CharsetEncoder encoder;
	final IdleStrategy idle;
	final byte[][] levels;
	final byte[] thread;

	float ratio;

	Local(final Charset charset, final IdleStrategy idle, final byte[][] levels) {
		this.encoder = charset.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		this.idle = idle;
		this.levels = levels;
		this.thread = Thread.currentThread().getName().getBytes(charset);
		this.ratio = this.encoder.averageBytesPerChar();
	}

	byte[] level(final Level level) {
		return this.levels[level.toInt()];
	}

	CharSequence format(final String format) {
		return format;
	}

	CharSequence format(final String format, final Object arg) {
		try {
			this.args[0] = arg;
			return format == null ? null : LoggerUtil.format(format, this.args, 1, this.dejaVu, this.message);
		} finally {
			Arrays.fill(this.args, null);
		}
	}

	CharSequence format(final String format, final Object arg1, final Object arg2) {
		try {
			this.args[0] = arg1;
			this.args[1] = arg2;
			return LoggerUtil.format(format, this.args, 2, this.dejaVu, this.message);
		} finally {
			Arrays.fill(this.args, null);
		}
	}

	CharSequence format(final String format, final Object[] args, final int length) {
		return LoggerUtil.format(format, args, length, this.dejaVu, this.message);
	}

	CharSequence format(final Throwable throwable) {
		return LoggerUtil.format(throwable, this.dejaVu, this.throwable);
	}

	boolean encode(final CharSequence string, final ByteBuffer buffer) {
		final CharBuffer chars = CharBuffer.wrap(string);
		try {
			CoderResult result = this.encoder.encode(chars, buffer, true);
			if (result.isUnderflow()) {
				result = this.encoder.flush(buffer);
			}
			return result.isUnderflow();
		} finally {
			this.encoder.reset();
		}
	}

	@Override
	public void close() {
		this.message.setLength(0);
	}

}
