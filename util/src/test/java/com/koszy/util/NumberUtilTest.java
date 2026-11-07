package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NumberUtilTest {

	@Test
	void test() {
		Assertions.assertDoesNotThrow(() -> NumberUtil.min(1, 2, 3));
	}

	@Test
	void digitsTest() {
		Assertions.assertEquals(1, NumberUtil.digits(0));
		Assertions.assertEquals(1, NumberUtil.digits(0L));

		Assertions.assertEquals(1, NumberUtil.digits(1));
		Assertions.assertEquals(1, NumberUtil.digits(1L));

		Assertions.assertEquals(1, NumberUtil.digits(-1));
		Assertions.assertEquals(1, NumberUtil.digits(-1L));

		Assertions.assertEquals(2, NumberUtil.digits(12));
		Assertions.assertEquals(2, NumberUtil.digits(12L));

		Assertions.assertEquals(2, NumberUtil.digits(-12));
		Assertions.assertEquals(2, NumberUtil.digits(-12L));

		Assertions.assertEquals(4, NumberUtil.digits(1234));
		Assertions.assertEquals(4, NumberUtil.digits(1234L));

		Assertions.assertEquals(4, NumberUtil.digits(-1234));
		Assertions.assertEquals(4, NumberUtil.digits(-1234L));

		Assertions.assertEquals(8, NumberUtil.digits(12345678));
		Assertions.assertEquals(8, NumberUtil.digits(12345678L));

		Assertions.assertEquals(8, NumberUtil.digits(-12345678));
		Assertions.assertEquals(8, NumberUtil.digits(-12345678L));

		Assertions.assertEquals(10, NumberUtil.digits(Integer.MAX_VALUE));
		Assertions.assertEquals(19, NumberUtil.digits(Long.MAX_VALUE));

		Assertions.assertEquals(10, NumberUtil.digits(Integer.MIN_VALUE));
		Assertions.assertEquals(19, NumberUtil.digits(Long.MAX_VALUE));
	}

}
