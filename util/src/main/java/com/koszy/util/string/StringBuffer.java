package com.koszy.util.string;

import java.util.Objects;
import java.util.stream.IntStream;

public final class StringBuffer implements MutableCharSequence {

	final java.lang.StringBuffer buffer;

	public StringBuffer() {
		this.buffer = new java.lang.StringBuffer();
	}

	public StringBuffer(final int capacity) {
		this.buffer = new java.lang.StringBuffer(capacity);
	}

	public StringBuffer(final CharSequence string) {
		this.buffer = new java.lang.StringBuffer(string);
	}

	public StringBuffer(final String string) {
		this.buffer = new java.lang.StringBuffer(string);
	}


	@Override
	public int length() {
		return this.buffer.length();
	}

	@Override
	public char charAt(final int index) {
		return this.buffer.charAt(index);
	}

	@Override
	public CharSequence subSequence(final int begin, final int end) {
		return this.buffer.subSequence(begin, end);
	}


	@Override
	public StringBuffer append(final boolean b) {
		this.buffer.append(b);
		return this;
	}

	@Override
	public StringBuffer append(final char c) {
		this.buffer.append(c);
		return this;
	}

	@Override
	public MutableCharSequence appendCodePoint(final int c) {
		this.buffer.appendCodePoint(c);
		return this;
	}

	@Override
	public StringBuffer append(final byte b) {
		this.buffer.append(b);
		return this;
	}

	@Override
	public StringBuffer append(final short s) {
		this.buffer.append(s);
		return this;
	}

	@Override
	public StringBuffer append(final int i) {
		this.buffer.append(i);
		return this;
	}

	@Override
	public StringBuffer append(final long l) {
		this.buffer.append(l);
		return this;
	}

	@Override
	public StringBuffer append(final float f) {
		this.buffer.append(f);
		return this;
	}

	@Override
	public StringBuffer append(final double d) {
		this.buffer.append(d);
		return this;
	}

	@Override
	public StringBuffer append(final CharSequence string) {
		if (string instanceof String) {
			this.buffer.append((String) string);
		} else if (string instanceof StringBuffer) {
			this.buffer.append(((StringBuffer) string).buffer);
		} else if (string instanceof StringBuilder) {
			this.buffer.append(((StringBuilder) string).builder);
		} else {
			this.buffer.append(string);
		}
		return this;
	}

	@Override
	public StringBuffer append(final CharSequence string, final int begin, final int end) {
		if (string instanceof String) {
			this.buffer.append((String) string, begin, end);
		} else if (string instanceof StringBuffer) {
			this.buffer.append(((StringBuffer) string).buffer, begin, end);
		} else if (string instanceof StringBuilder) {
			this.buffer.append(((StringBuilder) string).builder, begin, end);
		} else {
			this.buffer.append(string, begin, end);
		}
		return this;
	}

	@Override
	public StringBuffer append(final char[] string) {
		this.buffer.append(string);
		return this;
	}

	@Override
	public StringBuffer append(final char[] string, final int offset, final int length) {
		this.buffer.append(string, offset, length);
		return this;
	}

	public StringBuffer append(final String string) {
		this.buffer.append(string);
		return this;
	}

	public StringBuffer append(final String string, final int begin, final int end) {
		this.buffer.append(string, begin, end);
		return this;
	}

	public StringBuffer append(final StringBuffer string) {
		this.buffer.append(string == null ? null : string.buffer);
		return this;
	}

	public StringBuffer append(final StringBuffer string, final int begin, final int end) {
		this.buffer.append(string == null ? null : string.buffer, begin, end);
		return this;
	}

	public StringBuffer append(final java.lang.StringBuffer string) {
		this.buffer.append(string);
		return this;
	}

	public StringBuffer append(final java.lang.StringBuffer string, final int begin, final int end) {
		this.buffer.append(string, begin, end);
		return this;
	}

	public StringBuffer append(final StringBuilder string) {
		this.buffer.append(string == null ? null : string.builder);
		return this;
	}

	public StringBuffer append(final StringBuilder string, final int begin, final int end) {
		this.buffer.append(string == null ? null : string.builder, begin, end);
		return this;
	}

	public StringBuffer append(final java.lang.StringBuilder string) {
		this.buffer.append(string);
		return this;
	}

	public StringBuffer append(final java.lang.StringBuilder string, final int begin, final int end) {
		this.buffer.append(string, begin, end);
		return this;
	}

	@Override
	public StringBuffer append(final Object object) {
		this.buffer.append(object);
		return this;
	}


	@Override
	public StringBuffer length(final int length) {
		this.buffer.setLength(length);
		return this;
	}

	@Override
	public MutableCharSequence trim() {
		this.buffer.trimToSize();
		return this;
	}

	@Override
	public MutableCharSequence clear() {
		this.buffer.setLength(0);
		return this;
	}


	@Override
	public IntStream chars() {
		return this.buffer.chars();
	}

	@Override
	public IntStream codePoints() {
		return this.buffer.codePoints();
	}


	@Override
	public int hashCode() {
		return this.buffer.hashCode();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null || !Objects.equals(this.getClass(), object.getClass())) {
			return false;
		}
		final StringBuffer that = (StringBuffer) object;
		return Objects.equals(this.buffer, that.buffer);
	}


	@Override
	public String toString() {
		return this.buffer.toString();
	}

}
