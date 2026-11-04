package com.koszy.logger;

import org.slf4j.event.Level;
import org.slf4j.helpers.NormalizedParameters;

abstract class AbstractAppender implements Appender {

	final ThreadLocal<Local> local;

	AbstractAppender(final ThreadLocal<Local> local) {
		this.local = local;
	}

	abstract void append(Local local, Level level, CharSequence message, CharSequence throwable);

	@Override
	public final void append(final Level level, final String format) {
		try (final Local local = this.local.get()) {
			this.append(local, level, local.format(format), null);
		}
	}

	@Override
	public final void append(final Level level, final String format, final Object arg) {
		try (final Local local = this.local.get()) {
			if (arg instanceof Throwable) {
				this.append(local, level, local.format(format), local.format((Throwable) arg));
			} else {
				this.append(local, level, local.format(format, arg), null);
			}
		}
	}

	@Override
	public final void append(final Level level, final String format, final Throwable throwable) {
		try (final Local local = this.local.get()) {
			this.append(local, level, local.format(format), local.format(throwable));
		}
	}

	@Override
	public final void append(final Level level, final String format, final Object arg1, final Object arg2) {
		try (final Local local = this.local.get()) {
			if (arg2 instanceof Throwable) {
				this.append(local, level, local.format(format, arg1), local.format((Throwable) arg2));
			} else {
				this.append(local, level, local.format(format, arg1, arg2), null);
			}
		}
	}

	@Override
	public final void append(final Level level, final String format, final Object... args) {
		try (final Local local = this.local.get()) {
			final Throwable throwable = NormalizedParameters.getThrowableCandidate(args);
			if (throwable == null) {
				this.append(local, level, local.format(format, args, args == null ? 0 : args.length), null);
			} else {
				this.append(local, level, local.format(format, args, args.length - 1), local.format(throwable));
			}
		}
	}

}
