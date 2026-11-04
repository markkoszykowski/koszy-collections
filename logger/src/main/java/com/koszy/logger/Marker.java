package com.koszy.logger;

import com.koszy.util.Require;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Marker implements org.slf4j.Marker {
	final String name;
	final byte[] bytes;

	final List<org.slf4j.Marker> references = new CopyOnWriteArrayList<>();


	Marker(final String name) {
		this.name = name;
		this.bytes = name.getBytes();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void add(final org.slf4j.Marker reference) {
		Require.nonNull(reference);

	}

	@Override
	public boolean remove(final org.slf4j.Marker reference) {
		return false;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean hasReferences() {
		return false;
	}

	@Override
	public Iterator<org.slf4j.Marker> iterator() {
		return null;
	}

	@Override
	public boolean contains(final org.slf4j.Marker other) {
		return false;
	}

	@Override
	public boolean contains(final String name) {
		return false;
	}

}
