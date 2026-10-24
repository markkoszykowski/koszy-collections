package com.koszy.logger;

import java.nio.ByteBuffer;

interface Appender {

	void append(long time, byte[] thread, int level, byte[] logger, ByteBuffer message, ByteBuffer throwable);

}
