package com.koszy.logger;

import org.slf4j.Marker;
import org.slf4j.event.EventConstants;
import org.slf4j.event.Level;

public final class Logger implements org.slf4j.Logger {

	public static final Level DEFAULT = Level.INFO;

	static Logger[] EMPTY = new Logger[0];

	Logger[] children = EMPTY;

	final String name;
	final byte[] bytes;

	final Object lock = new Object();
	final int defaultt;
	volatile int level = -1;

	Logger(final String name, final int defaultt) {
		this.name = name;
		this.bytes = name.getBytes();
		this.defaultt = defaultt;
	}

	@Override
	public String getName() {
		return this.name;
	}

	boolean isEnabled(final int level) {
		return this.level <= level;
	}

	void setLevel(final int level) {
		synchronized (this.lock) {
			this.level = level;
			for (final Logger child : this.children) {
				child.setLevel(level);
			}
		}
	}

	public void setLevel(final Level level) {
		this.setLevel(level == null ? this.defaultt : level.toInt());
	}

	@Override
	public boolean isTraceEnabled() {
		return this.isEnabled(EventConstants.TRACE_INT);
	}

	@Override
	public void trace(final String msg) {
		if (this.isTraceEnabled()) {

		}
	}

	@Override
	public void trace(final String format, final Object arg) {

	}

	@Override
	public void trace(final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void trace(final String format, final Object... arguments) {

	}

	@Override
	public void trace(final String msg, final Throwable t) {

	}

	@Override
	public boolean isTraceEnabled(final Marker marker) {
		return false;
	}

	@Override
	public void trace(final Marker marker, final String msg) {

	}

	@Override
	public void trace(final Marker marker, final String format, final Object arg) {

	}

	@Override
	public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void trace(final Marker marker, final String format, final Object... argArray) {

	}

	@Override
	public void trace(final Marker marker, final String msg, final Throwable t) {

	}

	@Override
	public boolean isDebugEnabled() {
		return this.isEnabled(EventConstants.DEBUG_INT);
	}

	@Override
	public void debug(final String msg) {

	}

	@Override
	public void debug(final String format, final Object arg) {

	}

	@Override
	public void debug(final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void debug(final String format, final Object... arguments) {

	}

	@Override
	public void debug(final String msg, final Throwable t) {

	}

	@Override
	public boolean isDebugEnabled(final Marker marker) {
		return false;
	}

	@Override
	public void debug(final Marker marker, final String msg) {

	}

	@Override
	public void debug(final Marker marker, final String format, final Object arg) {

	}

	@Override
	public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void debug(final Marker marker, final String format, final Object... arguments) {

	}

	@Override
	public void debug(final Marker marker, final String msg, final Throwable t) {

	}

	@Override
	public boolean isInfoEnabled() {
		return this.isEnabled(EventConstants.INFO_INT);
	}

	@Override
	public void info(final String msg) {

	}

	@Override
	public void info(final String format, final Object arg) {

	}

	@Override
	public void info(final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void info(final String format, final Object... arguments) {

	}

	@Override
	public void info(final String msg, final Throwable t) {

	}

	@Override
	public boolean isInfoEnabled(final Marker marker) {
		return false;
	}

	@Override
	public void info(final Marker marker, final String msg) {

	}

	@Override
	public void info(final Marker marker, final String format, final Object arg) {

	}

	@Override
	public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void info(final Marker marker, final String format, final Object... arguments) {

	}

	@Override
	public void info(final Marker marker, final String msg, final Throwable t) {

	}

	@Override
	public boolean isWarnEnabled() {
		return this.isEnabled(EventConstants.WARN_INT);
	}

	@Override
	public void warn(final String msg) {

	}

	@Override
	public void warn(final String format, final Object arg) {

	}

	@Override
	public void warn(final String format, final Object... arguments) {

	}

	@Override
	public void warn(final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void warn(final String msg, final Throwable t) {

	}

	@Override
	public boolean isWarnEnabled(final Marker marker) {
		return false;
	}

	@Override
	public void warn(final Marker marker, final String msg) {

	}

	@Override
	public void warn(final Marker marker, final String format, final Object arg) {

	}

	@Override
	public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void warn(final Marker marker, final String format, final Object... arguments) {

	}

	@Override
	public void warn(final Marker marker, final String msg, final Throwable t) {

	}

	@Override
	public boolean isErrorEnabled() {
		return this.isEnabled(EventConstants.ERROR_INT);
	}

	@Override
	public void error(final String msg) {

	}

	@Override
	public void error(final String format, final Object arg) {

	}

	@Override
	public void error(final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void error(final String format, final Object... arguments) {

	}

	@Override
	public void error(final String msg, final Throwable t) {

	}

	@Override
	public boolean isErrorEnabled(final Marker marker) {
		return false;
	}

	@Override
	public void error(final Marker marker, final String msg) {

	}

	@Override
	public void error(final Marker marker, final String format, final Object arg) {

	}

	@Override
	public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {

	}

	@Override
	public void error(final Marker marker, final String format, final Object... arguments) {

	}

	@Override
	public void error(final Marker marker, final String msg, final Throwable t) {

	}

}
