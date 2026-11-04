package com.koszy.logger;

import com.koszy.util.ArrayUtil;
import com.koszy.util.Require;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class LoggerFactory implements ILoggerFactory {

	static final Object LOCK = new Object();
	static LoggerFactory instance = null;

	static LoggerFactory factory(final Configuration configuration) {
		synchronized (LOCK) {
			if (instance == null) {
				instance = new LoggerFactory(configuration);
			}
			return instance;
		}
	}

	static Level level(final Configuration configuration) {
		if (configuration == null) {
			return com.koszy.logger.Logger.DEFAULT;
		}
		final Level level = configuration.level();
		if (level == null) {
			return com.koszy.logger.Logger.DEFAULT;
		}
		return level;
	}

	static int level(final Configuration configuration, final com.koszy.logger.Logger parent, final String name) {
		if (configuration == null) {
			return parent.defaultt;
		}
		final Map<String, Level> levels = configuration.levels();
		if (levels == null) {
			return parent.defaultt;
		}
		final Level level = levels.get(name);
		if (level == null) {
			return parent.defaultt;
		}
		return level.toInt();
	}

	static int index(final String name, final int start) {
		final int dollar = name.indexOf(LoggerUtil.DOLLAR, start);
		final int dot = name.indexOf(LoggerUtil.DOT, start);
		if (dollar == -1 && dot == -1) {
			return -1;
		} else if (dollar == -1) {
			return dot;
		} else if (dot == -1) {
			return dollar;
		} else {
			return Math.min(dollar, dot);
		}
	}

	static String name(final String name, final int index) {
		return index == -1 ? name : name.substring(0, index);
	}

	static com.koszy.logger.Logger child(final Configuration configuration, final com.koszy.logger.Logger logger, final String name) {
		synchronized (logger.lock) {
			final com.koszy.logger.Logger[] children = logger.children;
			for (final com.koszy.logger.Logger child : children) {
				if (Objects.equals(name, child.name)) {
					return child;
				}
			}
			logger.children = ArrayUtil.copyOf(com.koszy.logger.Logger[]::new, children, children.length + 1);
			return logger.children[children.length] = new com.koszy.logger.Logger(name, level(configuration, logger, name));
		}
	}

	final Configuration configuration;
	final com.koszy.logger.Logger root;
	final Map<String, com.koszy.logger.Logger> cache = new ConcurrentHashMap<>();


	LoggerFactory(final Configuration configuration) {
		this.configuration = configuration;
		this.root = new com.koszy.logger.Logger(Logger.ROOT_LOGGER_NAME, level(configuration).toInt());
		this.cache.put(Logger.ROOT_LOGGER_NAME, this.root);
	}

	@Override
	public Logger getLogger(final String name) {
		Require.nonNull(name);

		com.koszy.logger.Logger logger = this.cache.get(name);
		if (logger != null) {
			return logger;
		}

		int start = 0;
		logger = this.root;
		do {
			final int index = index(name, start);
			start = index + 1;

			final String child = name(name, index);
			logger = child(this.configuration, logger, child);

			this.cache.put(child, logger);
		} while (start != 0);

		return logger;
	}

}
