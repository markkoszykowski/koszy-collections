package com.koszy.logger;

import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

public interface Formatter {

	boolean format(long time, byte[] thread, int level, byte[] logger, ByteBuffer message, ByteBuffer throwable, GatheringByteChannel channel);

}
