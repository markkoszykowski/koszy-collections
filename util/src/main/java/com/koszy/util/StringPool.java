package com.koszy.util;

public class StringPool {

	public static String valueOf(final char c) {
		return Characters.valueOf(c);
	}

	public static String valueOf(final byte b) {
		return Numbers.valueOf(b);
	}

	public static String valueOf(final short s) {
		return Numbers.valueOf(s);
	}

	public static String valueOf(final int i) {
		return Numbers.valueOf(i);
	}

	public static String valueOf(final long l) {
		return Numbers.valueOf(l);
	}

	private enum Characters {
		INSTANCE;

		final String[] strings;

		Characters() {
			this.strings = new String[Character.MAX_VALUE - Character.MIN_VALUE + 1];
			char c = Character.MIN_VALUE;
			for (int i = 0; i < this.strings.length; ++i) {
				this.strings[i] = String.valueOf(c++).intern();
			}
		}

		static String valueOf(final char c) {
			return INSTANCE.strings[c - Character.MIN_VALUE];
		}
	}

	private enum Numbers {
		INSTANCE;

		final String[] strings;

		Numbers() {
			this.strings = new String[Short.MAX_VALUE - Short.MIN_VALUE + 1];
			short s = Short.MIN_VALUE;
			for (int i = 0; i < this.strings.length; ++i) {
				this.strings[i] = String.valueOf(s++).intern();
			}
		}

		static String valueOf(final byte b) {
			return INSTANCE.strings[b - Short.MIN_VALUE];
		}

		static String valueOf(final short s) {
			return INSTANCE.strings[s - Short.MIN_VALUE];
		}

		static String valueOf(final int i) {
			return Short.MIN_VALUE <= i && i <= Short.MAX_VALUE ? INSTANCE.strings[i - Short.MIN_VALUE] : String.valueOf(i);
		}

		static String valueOf(final long l) {
			return Short.MIN_VALUE <= l && l <= Short.MAX_VALUE ? INSTANCE.strings[(int) (l - Short.MIN_VALUE)] : String.valueOf(l);
		}
	}

}
