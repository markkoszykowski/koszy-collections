package com.koszy.util.string;

import java.util.Objects;
import java.util.stream.IntStream;

public final class StringBuilder implements MutableCharSequence {

	final java.lang.StringBuilder builder;

	public StringBuilder() {
		this.builder = new java.lang.StringBuilder();
	}

	public StringBuilder(final int capacity) {
		this.builder = new java.lang.StringBuilder(capacity);
	}

	public StringBuilder(final CharSequence string) {
		this.builder = new java.lang.StringBuilder(string);
	}

	public StringBuilder(final String string) {
		this.builder = new java.lang.StringBuilder(string);
	}


	@Override
	public int length() {
		return this.builder.length();
	}

	@Override
	public char charAt(final int index) {
		return this.builder.charAt(index);
	}

	@Override
	public CharSequence subSequence(final int begin, final int end) {
		return this.builder.subSequence(begin, end);
	}


	@Override
	public StringBuilder append(final boolean b) {
		this.builder.append(b);
		return this;
	}

	@Override
	public StringBuilder append(final char c) {
		this.builder.append(c);
		return this;
	}

	@Override
	public MutableCharSequence appendCodePoint(final int c) {
		this.builder.appendCodePoint(c);
		return this;
	}

	@Override
	public StringBuilder append(final int i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public StringBuilder append(final long l) {
		this.builder.append(l);
		return this;
	}

	@Override
	public StringBuilder append(final float f) {
		this.builder.append(f);
		return this;
	}

	@Override
	public StringBuilder append(final double d) {
		this.builder.append(d);
		return this;
	}

	@Override
	public StringBuilder append(final CharSequence string) {
		if (string instanceof String) {
			this.builder.append((String) string);
		} else if (string instanceof StringBuilder) {
			this.builder.append(((StringBuilder) string).builder);
		} else if (string instanceof StringBuffer) {
			this.builder.append(((StringBuffer) string).buffer);
		} else {
			this.builder.append(string);
		}
		return this;
	}

	@Override
	public StringBuilder append(final CharSequence string, final int begin, final int end) {
		if (string instanceof String) {
			this.builder.append(string, begin, end);
		} else if (string instanceof StringBuilder) {
			this.builder.append(((StringBuilder) string).builder, begin, end);
		} else if (string instanceof StringBuffer) {
			this.builder.append(((StringBuffer) string).buffer, begin, end);
		} else {
			this.builder.append(string, begin, end);
		}
		return this;
	}

	@Override
	public StringBuilder append(final char[] string) {
		this.builder.append(string);
		return this;
	}

	@Override
	public StringBuilder append(final char[] string, final int offset, final int length) {
		this.builder.append(string, offset, length);
		return this;
	}

	public StringBuilder append(final String string) {
		this.builder.append(string);
		return this;
	}

	public StringBuilder append(final String string, final int begin, final int end) {
		this.builder.append(string, begin, end);
		return this;
	}

	public StringBuilder append(final StringBuilder string) {
		this.builder.append(string == null ? null : string.builder);
		return this;
	}

	public StringBuilder append(final StringBuilder string, final int begin, final int end) {
		this.builder.append(string == null ? null : string.builder, begin, end);
		return this;
	}

	public StringBuilder append(final java.lang.StringBuilder string) {
		this.builder.append(string);
		return this;
	}

	public StringBuilder append(final java.lang.StringBuilder string, final int begin, final int end) {
		this.builder.append(string, begin, end);
		return this;
	}

	public StringBuilder append(final StringBuffer string) {
		this.builder.append(string == null ? null : string.buffer);
		return this;
	}

	public StringBuilder append(final StringBuffer string, final int begin, final int end) {
		this.builder.append(string == null ? null : string.buffer, begin, end);
		return this;
	}

	public StringBuilder append(final java.lang.StringBuffer string) {
		this.builder.append(string);
		return this;
	}

	public StringBuilder append(final java.lang.StringBuffer string, final int begin, final int end) {
		this.builder.append(string, begin, end);
		return this;
	}

	@Override
	public StringBuilder append(final Object object) {
		this.builder.append(object);
		return this;
	}


	@Override
	public StringBuilder length(final int length) {
		this.builder.setLength(length);
		return this;
	}

	@Override
	public MutableCharSequence trim() {
		this.builder.trimToSize();
		return this;
	}

	@Override
	public MutableCharSequence clear() {
		this.builder.setLength(0);
		return this;
	}


	@Override
	public IntStream chars() {
		return this.builder.chars();
	}

	@Override
	public IntStream codePoints() {
		return this.builder.codePoints();
	}


	@Override
	public int hashCode() {
		return this.builder.hashCode();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null || !Objects.equals(this.getClass(), object.getClass())) {
			return false;
		}
		final StringBuilder that = (StringBuilder) object;
		return Objects.equals(this.builder, that.builder);
	}


	@Override
	public String toString() {
		return this.builder.toString();
	}

}
