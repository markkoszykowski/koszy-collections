package com.koszy.util;

import jdk.internal.math.FloatingDecimal;

public final class FloatUtil {

	public static void append(final float f, final Appendable appendable) {
		FloatingDecimal.appendTo(f, appendable);
	}

	public static void append(final double d, final Appendable appendable) {
		FloatingDecimal.appendTo(d, appendable);
	}

	private FloatUtil() {
		LangUtil.unreachable();
	}

}
