package com.koszy.util;

import org.junit.jupiter.api.Test;

class OpenUnsafeTest {

	@Test
	void test() {
		System.err.println(OpenUnsafe.theUnsafe().pageSize());
	}

}
