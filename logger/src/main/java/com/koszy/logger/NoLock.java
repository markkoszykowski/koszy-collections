package com.koszy.logger;

import com.koszy.util.Require;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public enum NoLock implements Lock {

	INSTANCE;

	public static NoLock instance() {
		return INSTANCE;
	}

	@Override
	public void lock() {
	}

	@Override
	public void lockInterruptibly() {
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public boolean tryLock(final long time, final TimeUnit unit) {
		Require.notNull(unit);
		return true;
	}

	@Override
	public void unlock() {
	}

	@Override
	public Condition newCondition() {
		return SignalledCondition.INSTANCE;
	}

	private enum SignalledCondition implements Condition {
		INSTANCE;

		public static SignalledCondition instance() {
			return INSTANCE;
		}

		@Override
		public void await() {
		}

		@Override
		public void awaitUninterruptibly() {
		}

		@Override
		public long awaitNanos(final long nanosTimeout) {
			return nanosTimeout;
		}

		@Override
		public boolean await(final long time, final TimeUnit unit) {
			return true;
		}

		@Override
		public boolean awaitUntil(final Date deadline) {
			return true;
		}

		@Override
		public void signal() {
		}

		@Override
		public void signalAll() {
		}
	}

}
