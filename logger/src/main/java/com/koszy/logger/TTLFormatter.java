package com.koszy.logger;

import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

public class TTLFormatter implements Formatter {

	@Override
	public void format(final long time, final byte[] thread, final int level, final byte[] logger, final ByteBuffer message, final ByteBuffer throwable, final GatheringByteChannel channel) {

	}

}
