package com.koszy.logger;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.slf4j.event.Level;

import java.util.Collections;
import java.util.Map;

final class ImmutableConfiguration implements Configuration {

	static ImmutableConfiguration from(final Configuration configuration) {
		return configuration instanceof ImmutableConfiguration ? (ImmutableConfiguration) configuration : new ImmutableConfiguration(configuration);
	}

	static boolean clone(final boolean value) {
		return value;
	}

	static <T> Class<T> clone(final Class<T> clazz) {
		return clazz;
	}

	static <T extends Enum<T>> T clone(final T value) {
		return value;
	}

	static Map<String, Level> clone(final Map<String, Level> levels) {
		return levels == null ? null : Collections.unmodifiableMap(new Object2ObjectOpenHashMap<>(levels));
	}

	final Level level;
	final Map<String, Level> levels;
	final boolean async;
	final Class<? extends Formatter> formatter;

	ImmutableConfiguration(final Configuration configuration) {
		this.level = clone(configuration.level());
		this.levels = clone(configuration.levels());
		this.async = clone(configuration.async());
		this.formatter = clone(configuration.formatter());
	}

	@Override
	public Level level() {
		return this.level;
	}

	@Override
	public Map<String, Level> levels() {
		return this.levels;
	}

	@Override
	public boolean async() {
		return this.async;
	}

	@Override
	public Class<? extends Formatter> formatter() {
		return this.formatter;
	}

}
