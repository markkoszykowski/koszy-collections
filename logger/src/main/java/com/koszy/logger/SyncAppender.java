package com.koszy.logger;

import org.slf4j.event.Level;

import java.nio.channels.GatheringByteChannel;

final class SyncAppender extends AbstractAppender {

	final Object lock;
	final GatheringByteChannel channel;

	SyncAppender(
			final ThreadLocal<Local> local,
			final Object lock,
			final GatheringByteChannel channel
	) {
		super(local);
		this.lock = lock;
		this.channel = channel;
	}

	@Override
	void append(final Local local, final Level level, final CharSequence message, final CharSequence throwable) {
		synchronized (this.lock) {
			this.channel.write() {

			}
			local.idle;
		}
	}

}
