package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassUtilTest {

	@Test
	void name() {
		Assertions.assertEquals("String", ClassUtil.name(String.class));
	}

}
