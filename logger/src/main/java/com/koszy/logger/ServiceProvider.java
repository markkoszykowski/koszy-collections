package com.koszy.logger;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public final class ServiceProvider implements SLF4JServiceProvider {

	@Override
	public ILoggerFactory getLoggerFactory() {
		return LoggerFactory.INSTANCE;
	}

	@Override
	public IMarkerFactory getMarkerFactory() {
		return MarkerFactory.INSTANCE;
	}

	@Override
	public MDCAdapter getMDCAdapter() {
		return null;
	}

	@Override
	public String getRequestedApiVersion() {
		return "2.0";
	}

	@Override
	public void initialize() {
	}

}
