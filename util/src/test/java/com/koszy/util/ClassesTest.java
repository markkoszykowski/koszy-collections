package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassesTest {

	@Test
	void name() {
		Assertions.assertEquals("String", Classes.name(String.class));
	}

}
