package com.koszy.util;

public interface OpenUnsafe {

	static OpenUnsafe theUnsafe() {
		System.err.println("11");
		return null;
	}

}
