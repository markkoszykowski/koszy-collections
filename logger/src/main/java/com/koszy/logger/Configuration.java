package com.koszy.logger;

import org.slf4j.event.Level;

import java.util.Map;

public interface Configuration {

	static Configuration configuration() {
		return null;
	}

	Level level();

	Map<String, Level> levels();

	boolean async();

	Class<? extends Formatter> formatter();

}
