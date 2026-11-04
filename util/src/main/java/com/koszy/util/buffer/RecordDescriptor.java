package com.koszy.util.buffer;

import com.koszy.util.LangUtil;
import com.koszy.util.NumberUtil;

public final class RecordDescriptor {

	public static final int HEADER_LENGTH = NumberUtil.INT_BYTES + NumberUtil.INT_BYTES;

	public static final int ALIGNMENT = HEADER_LENGTH;

	public static int lengthOffset(final int offset) {
		return offset;
	}

	public static int typeOffset(final int offset) {
		return offset + NumberUtil.INT_BYTES;
	}

	public static int messageOffset(final int offset) {
		return offset + NumberUtil.INT_BYTES + NumberUtil.INT_BYTES;
	}

	public static void checkType(final int type) {
		if (type < 1) {
			throw new IllegalArgumentException("message type id must be greater than zero, type=" + type);
		}
	}

	private RecordDescriptor() {
		LangUtil.unreachable();
	}

}
