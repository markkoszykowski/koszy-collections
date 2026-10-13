package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NumberUtilTest {

	@Test
	void test() {
		Assertions.assertDoesNotThrow(() -> NumberUtil.min(1, 2, 3));
	}

}
