package com.koszy.util.hash;

import com.koszy.util.ArrayUtil;
import it.unimi.dsi.fastutil.Hash;

public final class ArrayHashStrategy implements Hash.Strategy<Object> {

	public static final ArrayHashStrategy INSTANCE = new ArrayHashStrategy();

	public static ArrayHashStrategy instance() {
		return INSTANCE;
	}

	@Override
	public int hashCode(final Object object) {
		return ArrayUtil.deepHashCode(object);
	}

	@Override
	public boolean equals(final Object left, final Object right) {
		return ArrayUtil.deepEquals(left, right);
	}

	private ArrayHashStrategy() {
	}

}
