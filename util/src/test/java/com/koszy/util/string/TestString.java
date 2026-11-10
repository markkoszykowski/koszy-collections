package com.koszy.util.string;

import com.koszy.util.Require;

import java.util.Arrays;

final class TestString implements CharSequence {

	final int length;

	TestString(final int length) {
		this.length = length;
	}

	@Override
	public int length() {
		return this.length;
	}

	@Override
	public char charAt(final int index) {
		Require.checkIndex(index, this.length);
		return '\0';
	}

	@Override
	public CharSequence subSequence(final int begin, final int end) {
		Require.checkBeginEnd(begin, end, this.length);
		return new TestString(end - begin);
	}

	@Override
	public String toString() {
		final char[] string = new char[this.length];
		Arrays.fill(string, '\0');
		return new String(string);
	}

}
