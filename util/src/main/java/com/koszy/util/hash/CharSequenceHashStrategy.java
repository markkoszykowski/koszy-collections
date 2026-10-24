package com.koszy.util.hash;

import com.koszy.util.StringUtil;
import it.unimi.dsi.fastutil.Hash;

public final class CharSequenceHashStrategy implements Hash.Strategy<CharSequence> {

	public static final CharSequenceHashStrategy INSTANCE = new CharSequenceHashStrategy();

	public static CharSequenceHashStrategy instance() {
		return INSTANCE;
	}

	@Override
	public int hashCode(final CharSequence string) {
		return StringUtil.hashCode(string);
	}

	@Override
	public boolean equals(final CharSequence left, final CharSequence right) {
		return StringUtil.equals(left, right);
	}

	private CharSequenceHashStrategy() {
	}

}
