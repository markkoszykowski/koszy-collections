package com.koszy.util.string;

import com.koszy.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.StringBuilder;
import java.util.Random;

class UnsafeCharSequenceTest {

	static final Random RANDOM = new Random();

	static String randomLatin1() {
		return randomLatin1(RANDOM.nextInt((1 << 16) + 1));
	}

	static String randomUtf16() {
		return randomUtf16(RANDOM.nextInt((1 << 16) + 1));
	}

	static String randomLatin1(final int length) {
		final StringBuilder string = new StringBuilder(length);
		for (int i = 0; i != length; ++i) {
			string.append(StringUtil.toLatin1Char((byte) RANDOM.nextInt()));
		}
		return string.toString();
	}

	static String randomUtf16(final int length) {
		final StringBuilder string = new StringBuilder(length);
		for (int i = 0; i != length; ++i) {
			string.append((char) RANDOM.nextInt());
		}
		return string.toString();
	}

	@Test
	void constructorTest() {
//		Assertions.assertEquals("", new UnsafeCharSequence().toString());
//		Assertions.assertEquals("", new UnsafeCharSequence(1).toString());
//		Assertions.assertEquals("", new UnsafeCharSequence("").toString());
//		Assertions.assertEquals("", new UnsafeCharSequence(new UnsafeCharSequence()).toString());

		String string;

//		string = randomLatin1();
//		Assertions.assertEquals(string, new UnsafeCharSequence(string).toString());

		string = randomUtf16(128);
		Assertions.assertEquals(string, new UnsafeCharSequence(string).toString());
	}

}
