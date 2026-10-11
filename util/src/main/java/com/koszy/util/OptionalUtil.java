package com.koszy.util;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class OptionalUtil {


	public static OptionalInt ofNullable(final Integer i) {
		return i == null ? OptionalInt.empty() : OptionalInt.of(i);
	}

	public static OptionalInt ofNullable(final int i) {
		return OptionalInt.of(i);
	}

	public static OptionalLong ofNullable(final Long l) {
		return l == null ? OptionalLong.empty() : OptionalLong.of(l);
	}

	public static OptionalLong ofNullable(final long l) {
		return OptionalLong.of(l);
	}

	public static OptionalDouble ofNullable(final Double d) {
		return d == null ? OptionalDouble.empty() : OptionalDouble.of(d);
	}

	public static OptionalDouble ofNullable(final double d) {
		return OptionalDouble.of(d);
	}


	public static Integer getNullable(final OptionalInt optional) {
		return optional.isPresent() ? optional.getAsInt() : null;
	}

	public static Long getNullable(final OptionalLong optional) {
		return optional.isPresent() ? optional.getAsLong() : null;
	}

	public static Double getNullable(final OptionalDouble optional) {
		return optional.isPresent() ? optional.getAsDouble() : null;
	}


	private OptionalUtil() {
		LangUtil.unreachable();
	}

}
