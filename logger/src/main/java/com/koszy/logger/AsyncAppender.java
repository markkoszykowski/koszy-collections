package com.koszy.logger;

import java.nio.ByteBuffer;

final class AsyncAppender implements Appender {

	@Override
	public void append(final long time, final byte[] thread, final int level, final byte[] logger, final ByteBuffer message, final ByteBuffer throwable) {

	}

}
