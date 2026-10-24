package com.koszy.logger;

import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

enum MarkerFactory implements IMarkerFactory {
	INSTANCE;

	@Override
	public Marker getMarker(final String name) {
		return null;
	}

	@Override
	public boolean exists(final String name) {
		return false;
	}

	@Override
	public boolean detachMarker(final String name) {
		return false;
	}

	@Override
	public Marker getDetachedMarker(final String name) {
		return null;
	}

}
