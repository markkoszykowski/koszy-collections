package com.koszy.logger;

import com.koszy.util.LangUtil;

final class ThreadUtil {

	static void waiting() {
		Thread.yield();
	}

	private ThreadUtil() {
		LangUtil.unreachable();
	}

}
