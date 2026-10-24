package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringPoolTest {

	@Test
	void test() {
		Assertions.assertSame(StringPool.valueOf((byte) 0), StringPool.valueOf(0L));

		Assertions.assertEquals(StringPool.valueOf(Long.MAX_VALUE), StringPool.valueOf(Long.MAX_VALUE));
	}

}
