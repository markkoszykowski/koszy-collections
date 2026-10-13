package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

class ClassUtilTest {

	@Test
	void name() {
		Assertions.assertEquals("String", ClassUtil.name(String.class));
	}

	@Test
	void sizeof() {
		Assertions.assertEquals(ClassLayout.parseClass(String.class).instanceSize(), ClassUtil.sizeof(String.class));
	}

}
