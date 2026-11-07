package com.koszy.util;

import java.io.IOException;

public final class NumberFormatUtil {

	/// @see sun.misc.FloatingDecimal
	private static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(() -> new StringBuilder(26));


	public static int length(final int i) {
		return (i < 0 ? 1 : 0) + NumberUtil.digits(i);
	}

	public static int length(final long l) {
		return (l < 0 ? 1 : 0) + NumberUtil.digits(l);
	}


	public static void append(final float f, final Appendable appendable) {
		final StringBuilder buffer = BUFFER.get();
		try {
			appendable.append(buffer.append(f));
		} catch (final IOException exception) {
			LangUtil.rethrow(exception);
		} finally {
			buffer.setLength(0);
		}
	}

	public static void append(final double d, final Appendable appendable) {
		final StringBuilder buffer = BUFFER.get();
		try {
			appendable.append(buffer.append(d));
		} catch (final IOException exception) {
			LangUtil.rethrow(exception);
		} finally {
			buffer.setLength(0);
		}
	}

	private NumberFormatUtil() {
		LangUtil.unreachable();
	}

}
