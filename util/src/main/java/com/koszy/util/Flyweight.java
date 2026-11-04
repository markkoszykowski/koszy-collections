package com.koszy.util;

public abstract class Flyweight<SELF> implements AutoCloseable {

	@SuppressWarnings(value = {"unchecked"})
	protected final SELF self() {
		return (SELF) this;
	}

	@Override
	public abstract void close();

}
