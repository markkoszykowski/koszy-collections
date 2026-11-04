package com.koszy.logger;

import org.slf4j.event.Level;

interface Appender {

	void append(Level level, String format);

	void append(Level level, String format, Object arg);

	void append(Level level, String format, Throwable throwable);

	void append(Level level, String format, Object arg1, Object arg2);

	void append(Level level, String format, Object... args);

}
