package com.koszy.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.charset.StandardCharsets;

class FileUtilTest {

	@Test
	void stdoutTest() {
//		for (int i = 0; i != 10_000; ++i) {
//			System.out.println("before " + i);
//		}
		try (final GatheringByteChannel stdout = FileUtil.stdout()) {
			stdout.write(ByteBuffer.wrap("one".getBytes(StandardCharsets.UTF_8)));
			stdout.write(ByteBuffer.wrap("two".getBytes(StandardCharsets.UTF_8)));
		} catch (final IOException exception) {
			LangUtil.rethrow(exception);
		}
//		for (int i = 0; i != 10_000; ++i) {
//			System.out.println("between " + i);
//		}
		try (final GatheringByteChannel stdout = FileUtil.stdout()) {
			stdout.write(ByteBuffer.wrap("two".getBytes(StandardCharsets.UTF_8)));
		} catch (final IOException exception) {
			LangUtil.rethrow(exception);
		}
//		for (int i = 0; i != 10_000; ++i) {
//			System.out.println("after " + i);
//		}
	}

}
