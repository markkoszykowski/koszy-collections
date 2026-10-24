package com.koszy.util.hash;

import com.koszy.util.LangUtil;
import com.koszy.util.ThrowableUtil;
import it.unimi.dsi.fastutil.Hash;

public final class ThrowableHashStrategy implements Hash.Strategy<Throwable> {

	public static final ThrowableHashStrategy INSTANCE = new ThrowableHashStrategy();

	public static ThrowableHashStrategy instance() {
		return INSTANCE;
	}

	@Override
	public int hashCode(final Throwable throwable) {
		return ThrowableUtil.hashCode(throwable);
	}

	@Override
	public boolean equals(final Throwable left, final Throwable right) {
		return ThrowableUtil.equals(left, right);
	}

	private ThrowableHashStrategy() {
		LangUtil.unreachable();
	}

}
