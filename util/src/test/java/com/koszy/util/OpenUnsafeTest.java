package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenUnsafeTest {

	@Test
	void test() {
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_BOOLEAN_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_BYTE_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_SHORT_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_CHAR_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_INT_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_LONG_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_FLOAT_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_DOUBLE_INDEX_SCALE);
		Assertions.assertDoesNotThrow(() -> OpenUnsafe.ARRAY_OBJECT_INDEX_SCALE);
	}

}
