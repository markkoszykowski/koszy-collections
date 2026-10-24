package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnumUtilTest {

	enum TestEnum {
		ONE,
		TWO,
	}

	@Test
	void valueOfTest() {
		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOf("ONE", TestEnum.values()));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOf("TWO", TestEnum.values()));

		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOf("ONE", TestEnum.class));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOf("TWO", TestEnum.class));

		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOf(" ONE ", 1, 3, TestEnum.values()));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOf(" TWO ", 1, 3, TestEnum.values()));

		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOf(" ONE ", 1, 3, TestEnum.class));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOf(" TWO ", 1, 3, TestEnum.class));

		Assertions.assertNull(EnumUtil.valueOf(null, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOf("", TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOf("one", TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOf("two", TestEnum.values()));

		Assertions.assertNull(EnumUtil.valueOf(null, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOf("", TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOf("one", TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOf("two", TestEnum.class));

		Assertions.assertNull(EnumUtil.valueOf(null, 0, 0, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOf("", 0, 0, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOf(" one ", 1, 3, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOf(" two ", 1, 3, TestEnum.values()));

		Assertions.assertNull(EnumUtil.valueOf(null, 0, 0, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOf("", 0, 0, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOf(" one ", 1, 3, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOf(" two ", 1, 3, TestEnum.class));

		Assertions.assertNull(EnumUtil.valueOf("ONE", (TestEnum[]) null));
		Assertions.assertNull(EnumUtil.valueOf("TWO", (TestEnum[]) null));
		Assertions.assertNull(EnumUtil.valueOf(" ONE ", 1, 3, (TestEnum[]) null));
		Assertions.assertNull(EnumUtil.valueOf(" TWO ", 1, 3, (TestEnum[]) null));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf(null, (Class<TestEnum>) null));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf(null, 0, 0, (Class<TestEnum>) null));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf(null, 1, 0, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf(null, 1, 0, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf(null, 0, 1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf(null, 0, 1, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", 1, 0, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", 1, 0, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", 0, 1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", 0, 1, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", -1, 1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", -1, 1, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", 1, -1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOf("", 1, -1, TestEnum.class));
	}

	@Test
	void valueOfIgnoreCaseTest() {
		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase("ONE", TestEnum.values()));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase("TWO", TestEnum.values()));
		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase("one", TestEnum.values()));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase("two", TestEnum.values()));

		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase("ONE", TestEnum.class));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase("TWO", TestEnum.class));
		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase("one", TestEnum.class));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase("two", TestEnum.class));

		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase(" ONE ", 1, 3, TestEnum.values()));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase(" TWO ", 1, 3, TestEnum.values()));
		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase(" one ", 1, 3, TestEnum.values()));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase(" two ", 1, 3, TestEnum.values()));

		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase(" ONE ", 1, 3, TestEnum.class));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase(" TWO ", 1, 3, TestEnum.class));
		Assertions.assertEquals(TestEnum.ONE, EnumUtil.valueOfIgnoreCase(" one ", 1, 3, TestEnum.class));
		Assertions.assertEquals(TestEnum.TWO, EnumUtil.valueOfIgnoreCase(" two ", 1, 3, TestEnum.class));

		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(null, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("", TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("111", TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("222", TestEnum.values()));

		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(null, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("", TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("111", TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("222", TestEnum.class));

		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(null, 0, 0, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("", 0, 0, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(" 111 ", 1, 3, TestEnum.values()));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(" 222 ", 1, 3, TestEnum.values()));

		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(null, 0, 0, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("", 0, 0, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(" 111 ", 1, 3, TestEnum.class));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(" 222 ", 1, 3, TestEnum.class));

		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("ONE", (TestEnum[]) null));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase("TWO", (TestEnum[]) null));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(" ONE ", 1, 3, (TestEnum[]) null));
		Assertions.assertNull(EnumUtil.valueOfIgnoreCase(" TWO ", 1, 3, (TestEnum[]) null));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase(null, (Class<TestEnum>) null));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase(null, 0, 0, (Class<TestEnum>) null));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase(null, 1, 0, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase(null, 1, 0, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase(null, 0, 1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase(null, 0, 1, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", 1, 0, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", 1, 0, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", 0, 1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", 0, 1, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", -1, 1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", -1, 1, TestEnum.class));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", 1, -1, TestEnum.values()));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> EnumUtil.valueOfIgnoreCase("", 1, -1, TestEnum.class));
	}

}
