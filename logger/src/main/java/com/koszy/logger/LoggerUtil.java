package com.koszy.logger;

import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class LoggerUtil {

	static final byte[] EMPTY = new byte[0];
	static final byte[][] LEVELS;

	static {
		final List<byte[]> levels = new ArrayList<>();
		for (final Level level : Level.values()) {
			final int value = level.toInt();
			while (levels.size() <= value) {
				levels.add(EMPTY);
			}
			levels.set(value, level.toString().getBytes());
		}
		LEVELS = levels.toArray(new byte[0][]);
	}

	static final String DELIM_STR = "{}";
	static final char DELIM_BEGIN = '{';
	static final char DELIM_END = '}';

	static final char ESCAPE = '\\';

	static final char DOLLAR = '$';
	static final char DOT = '.';

	/// @see Throwable#CAUSE_CAPTION
	private static final String CAUSE_CAPTION = "Caused by: ";

	/// @see Throwable#SUPPRESSED_CAPTION
	private static final String SUPPRESSED_CAPTION = "Suppressed: ";

	static byte[] level(final int level) {
		return 0 <= level && level < LEVELS.length ? LEVELS[level] : EMPTY;
	}

	static boolean isEscapedDelimiter(final String format, final int index) {
		return 1 <= index && format.charAt(index - 1) == ESCAPE;
	}

	static boolean isDoubleEscaped(final String format, final int index) {
		return 2 <= index && format.charAt(index - 2) == ESCAPE;
	}

	/// @see org.slf4j.helpers.MessageFormatter#arrayFormat(String, Object[], Throwable)
	static CharSequence format(final String format, final Object[] args, final int length, final Set<Object> dejaVu, final StringBuilder builder) {
		if (format == null) {
			return null;
		}
		int start = 0;
		int end;
		for (int i = 0; i != length; ++i) {
			end = format.indexOf(DELIM_STR, start);
			if (end == -1) {
				if (start == 0) {
					return format;
				} else {
					return builder.append(format, start, format.length());
				}
			} else {
				if (isEscapedDelimiter(format, end)) {
					if (isDoubleEscaped(format, end)) {
						builder.append(format, start, end - 1);
						append(args[i], dejaVu, builder);
						start = end + 2;
					} else {
						--i;
						builder.append(format, start, end - 1);
						builder.append(DELIM_BEGIN);
						start = end + 1;
					}
				} else {
					builder.append(format, start, end);
					append(args[i], dejaVu, builder);
					start = end + 2;
				}
			}
		}
		return builder;
	}

	public static StringBuilder indent(final StringBuilder builder, final int indent) {
		for (int i = 0; i != indent; ++i) {
			builder.append('\t');
		}
		return builder;
	}

	/// @see java.util.Arrays#toString(boolean[])
	public static StringBuilder append(final boolean[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final boolean element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(char[])
	public static StringBuilder append(final char[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final char element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(byte[])
	public static StringBuilder append(final byte[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final byte element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(short[])
	public static StringBuilder append(final short[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final short element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(int[])
	public static StringBuilder append(final int[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final int element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(long[])
	public static StringBuilder append(final long[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final long element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(float[])
	public static StringBuilder append(final float[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final float element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#toString(double[])
	public static StringBuilder append(final double[] array, final StringBuilder builder) {
		String prefix = null;
		builder.append('[');
		for (final double element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append(element);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');
		return builder;
	}

	/// @see java.util.Arrays#deepToString(Object[])
	public static StringBuilder append(final Object[] array, final StringBuilder builder, final Set<Object> dejaVu) {
		dejaVu.add(array);

		builder.append('[');
		String prefix = null;
		for (final Object element : array) {
			if (prefix != null) {
				builder.append(prefix);
			}
			append(element, dejaVu, builder);
			if (prefix == null) {
				prefix = ", ";
			}
		}
		builder.append(']');

		dejaVu.remove(array);

		return builder;
	}

	public static StringBuilder append(final Object object, final Set<Object> dejaVu, final StringBuilder builder) {
		if (object == null) {
			return builder.append((Object) null);
		}
		final Class<?> clazz = object.getClass();
		if (clazz.isArray()) {
			if (object instanceof boolean[]) {
				return append((boolean[]) object, builder);
			} else if (object instanceof char[]) {
				return append((char[]) object, builder);
			} else if (object instanceof byte[]) {
				return append((byte[]) object, builder);
			} else if (object instanceof short[]) {
				return append((short[]) object, builder);
			} else if (object instanceof int[]) {
				return append((int[]) object, builder);
			} else if (object instanceof long[]) {
				return append((long[]) object, builder);
			} else if (object instanceof float[]) {
				return append((float[]) object, builder);
			} else if (object instanceof double[]) {
				return append((double[]) object, builder);
			} else {
				if (dejaVu.contains(object)) {
					return builder.append("[...]");
				} else {
					return append((Object[]) object, builder, dejaVu);
				}
			}
		} else {
			if (object instanceof Boolean) {
				return builder.append((boolean) object);
			} else if (object instanceof Character) {
				return builder.append((char) object);
			} else if (object instanceof Byte) {
				return builder.append((byte) object);
			} else if (object instanceof Short) {
				return builder.append((short) object);
			} else if (object instanceof Integer) {
				return builder.append((int) object);
			} else if (object instanceof Long) {
				return builder.append((long) object);
			} else if (object instanceof Float) {
				return builder.append((float) object);
			} else if (object instanceof Double) {
				return builder.append((double) object);
			} else if (object instanceof CharSequence) {
				return builder.append((CharSequence) object);
			} else if (object instanceof Formattable) {
				((Formattable) object).format(builder);
				return builder;
			} else {
				return builder.append(object);
			}
		}
	}

	static CharSequence format(final Throwable throwable, final Set<? super Throwable> dejaVu, final StringBuilder builder) {
		try {
			return throwable == null ? null : append(throwable, dejaVu, builder, null, 0, null);
		} finally {
			dejaVu.clear();
		}
	}

	public static StringBuilder append(final Throwable throwable, final Set<? super Throwable> dejaVu, final StringBuilder builder) {
		try {
			return throwable == null ? builder.append((Object) null) : append(throwable, dejaVu, builder, null, 0, null);
		} finally {
			dejaVu.clear();
		}
	}

	/// @see Throwable#printStackTrace()
	static StringBuilder append(final Throwable throwable, final Set<? super Throwable> dejaVu, final StringBuilder builder, final StackTraceElement[] stack, final int indent, final String prefix) {
		if (dejaVu.add(throwable)) {
			indent(builder, indent);
			if (prefix != null) {
				builder.append(prefix);
			}
			ThrowableUtil.append(throwable, builder);
			builder.append(System.lineSeparator());

			final StackTraceElement[] trace = throwable.getStackTrace();
			if (trace != null) {
				if (stack == null) {
					for (final StackTraceElement element : trace) {
						if (element != null) {
							indent(builder, indent + 1).append("at ");
							ThrowableUtil.append(element, builder);
							builder.append(System.lineSeparator());
						}
					}
				} else {
					int i = stack.length - 1;
					int j = trace.length - 1;
					for (; 0 <= i && 0 <= j && Objects.equals(stack[i], trace[j]); --i, --j) {
					}
					final int common = trace.length - 1 - j;
					for (int k = 0; k <= j; ++k) {
						final StackTraceElement element = trace[k];
						if (element != null) {
							indent(builder, indent + 1).append("at ");
							ThrowableUtil.append(element, builder);
							builder.append(System.lineSeparator());
						}
					}
					if (0 < common) {
						indent(builder, indent + 1);
						builder.append("... ").append(common).append(" more");
						builder.append(System.lineSeparator());
					}
				}
			}

			final Throwable[] suppressed = throwable.getSuppressed();
			if (suppressed != null) {
				for (final Throwable suppress : suppressed) {
					if (suppress != null) {
						append(suppress, dejaVu, builder, stack, indent + 1, SUPPRESSED_CAPTION);
					}
				}
			}

			final Throwable cause = throwable.getCause();
			if (cause != null) {
				append(cause, dejaVu, builder, stack, indent + 1, CAUSE_CAPTION);
			}
		} else {
			indent(builder, indent);
			if (prefix != null) {
				builder.append(prefix);
			}
			builder.append("[CIRCULAR REFERENCE: ");
			ThrowableUtil.append(throwable, builder);
			builder.append(']');
			builder.append(System.lineSeparator());
		}
		return builder;
	}

}
