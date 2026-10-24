package com.koszy.logger;

import com.koszy.util.LangUtil;

final class ThreadUtil {

	static void waiting() {
		Thread.onSpinWait();
	}

	private ThreadUtil() {
		LangUtil.unreachable();
	}

}
