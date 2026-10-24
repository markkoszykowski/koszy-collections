package com.koszy.util;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

public final class FileUtil {

	public static ScatteringByteChannel stdin() {
		return StandardScatteringChannel.IN.channel();
	}

	public static GatheringByteChannel stdout() {
		return StandardGatheringChannel.OUT.channel();
	}

	public static GatheringByteChannel stderr() {
		return StandardGatheringChannel.ERR.channel();
	}

	private static final class StandardStreamScatteringByteChannel implements ScatteringByteChannel {
		private final ScatteringByteChannel channel;

		public StandardStreamScatteringByteChannel(final ScatteringByteChannel channel) {
			this.channel = channel;
		}

		@Override
		public int read(final ByteBuffer dst) throws IOException {
			return this.channel.read(dst);
		}

		@Override
		public long read(final ByteBuffer[] dsts) throws IOException {
			return this.channel.read(dsts);
		}

		@Override
		public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
			return this.channel.read(dsts, offset, length);
		}

		@Override
		public boolean isOpen() {
			return this.channel.isOpen();
		}

		@Override
		public void close() {
		}

		@Override
		public String toString() {
			return this.channel.toString();
		}
	}

	private static final class StandardStreamGatheringByteChannel implements GatheringByteChannel {
		private final GatheringByteChannel channel;

		private StandardStreamGatheringByteChannel(final GatheringByteChannel channel) {
			this.channel = channel;
		}

		@Override
		public int write(final ByteBuffer src) throws IOException {
			return this.channel.write(src);
		}

		@Override
		public long write(final ByteBuffer[] srcs) throws IOException {
			return this.channel.write(srcs);
		}

		@Override
		public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException {
			return this.channel.write(srcs, offset, length);
		}

		@Override
		public boolean isOpen() {
			return this.channel.isOpen();
		}

		@Override
		public void close() {
		}

		@Override
		public String toString() {
			return this.channel.toString();
		}
	}

	private enum StandardScatteringChannel {
		IN(FileDescriptor.in);

		private final ScatteringByteChannel channel;

		StandardScatteringChannel(final FileDescriptor fd) {
			this.channel = new StandardStreamScatteringByteChannel(new FileInputStream(fd).getChannel());
		}

		public ScatteringByteChannel channel() {
			return this.channel;
		}
	}

	private enum StandardGatheringChannel {
		OUT(FileDescriptor.out),
		ERR(FileDescriptor.err);

		private final GatheringByteChannel channel;

		StandardGatheringChannel(final FileDescriptor fd) {
			this.channel = new StandardStreamGatheringByteChannel(new FileOutputStream(fd).getChannel());
		}

		public GatheringByteChannel channel() {
			return this.channel;
		}
	}

	private FileUtil() {
		LangUtil.unreachable();
	}

}
