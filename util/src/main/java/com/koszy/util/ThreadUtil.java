package com.koszy.util;

public final class ThreadUtil {

	public static <T> T handle(final InterruptedException exception) {
		Require.notNull(exception);
		Thread.currentThread().interrupt();
		return LangUtil.rethrow(exception);
	}

	public static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException exception) {
			handle(exception);
		}
	}

	public static void sleep(final long millis, final int nanos) {
		try {
			Thread.sleep(millis, nanos);
		} catch (final InterruptedException exception) {
			handle(exception);
		}
	}

	public static boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}

	public static boolean interrupted() {
		try {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
		} catch (final InterruptedException exception) {
			LangUtil.rethrow(exception);
		}
		return false;
	}

	private ThreadUtil() {
		LangUtil.unreachable();
	}

}
