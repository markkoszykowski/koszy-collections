package com.koszy.logger;

import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.concurrent.locks.Lock;

final class SyncAppender implements Appender {

	final GatheringByteChannel channel;
	final Lock lock;
	final Formatter formatter;
	final Thread.UncaughtExceptionHandler handler;

	SyncAppender(
			final GatheringByteChannel channel,
			final Lock lock,
			final Formatter formatter,
			final Thread.UncaughtExceptionHandler handler
	) {
		this.channel = channel;
		this.lock = lock;
		this.formatter = formatter;
		this.handler = handler;
	}

	@Override
	public void append(final long time, final byte[] thread, final int level, final byte[] logger, final ByteBuffer message, final ByteBuffer throwable) {
		final Thread current = Thread.currentThread();
		this.lock.lock();
		try {
			while (!current.isInterrupted() && !this.formatter.format(time, thread, level, logger, message, throwable, this.channel)) {
				ThreadUtil.waiting();
			}
			this.formatter.format(time, thread, level, logger, message, throwable, this.channel);
		} catch (final Exception exception) {
			this.handler.uncaughtException(Thread.currentThread(), exception);
		} finally {
			this.lock.unlock();
		}
	}

}
