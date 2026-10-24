package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

class OptionalUtilTest {

	@Test
	void ofNullableTest() {
		Assertions.assertEquals(OptionalInt.empty(), OptionalUtil.ofNullable((Integer) null));
		Assertions.assertEquals(OptionalLong.empty(), OptionalUtil.ofNullable((Long) null));
		Assertions.assertEquals(OptionalDouble.empty(), OptionalUtil.ofNullable((Double) null));

		Assertions.assertEquals(OptionalInt.of(1), OptionalUtil.ofNullable(1));
		Assertions.assertEquals(OptionalInt.of(1), OptionalUtil.ofNullable((Integer) 1));
		Assertions.assertEquals(OptionalLong.of(1L), OptionalUtil.ofNullable(1L));
		Assertions.assertEquals(OptionalLong.of(1L), OptionalUtil.ofNullable((Long) 1L));
		Assertions.assertEquals(OptionalDouble.of(1.0), OptionalUtil.ofNullable(1.0));
		Assertions.assertEquals(OptionalDouble.of(1.0), OptionalUtil.ofNullable((Double) 1.0));
	}

	@Test
	void getNullableTest() {
		Assertions.assertNull(OptionalUtil.getNullable(OptionalInt.empty()));
		Assertions.assertNull(OptionalUtil.getNullable(OptionalLong.empty()));
		Assertions.assertNull(OptionalUtil.getNullable(OptionalDouble.empty()));

		Assertions.assertEquals(1, OptionalUtil.getNullable(OptionalInt.of(1)));
		Assertions.assertEquals(1L, OptionalUtil.getNullable(OptionalLong.of(1L)));
		Assertions.assertEquals(1.0, OptionalUtil.getNullable(OptionalDouble.of(1.0)));

		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.getNullable((OptionalInt) null));
		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.getNullable((OptionalLong) null));
		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.getNullable((OptionalDouble) null));
	}

	@Test
	void mapToObjTest() {
		Assertions.assertEquals(Optional.empty(), OptionalUtil.mapToObj(OptionalInt.empty()));
		Assertions.assertEquals(Optional.empty(), OptionalUtil.mapToObj(OptionalLong.empty()));
		Assertions.assertEquals(Optional.empty(), OptionalUtil.mapToObj(OptionalDouble.empty()));

		Assertions.assertEquals(Optional.of(1), OptionalUtil.mapToObj(OptionalInt.of(1)));
		Assertions.assertEquals(Optional.of(1L), OptionalUtil.mapToObj(OptionalLong.of(1L)));
		Assertions.assertEquals(Optional.of(1.0), OptionalUtil.mapToObj(OptionalDouble.of(1.0)));

		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.mapToObj((OptionalInt) null));
		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.mapToObj((OptionalLong) null));
		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.mapToObj((OptionalDouble) null));
	}


	@Test
	void mapToPrimitiveTest() {
		Assertions.assertEquals(OptionalInt.empty(), OptionalUtil.mapToInt(Optional.empty()));
		Assertions.assertEquals(OptionalLong.empty(), OptionalUtil.mapToLong(Optional.empty()));
		Assertions.assertEquals(OptionalDouble.empty(), OptionalUtil.mapToDouble(Optional.empty()));

		Assertions.assertEquals(OptionalInt.of(1), OptionalUtil.mapToInt(Optional.of(1)));
		Assertions.assertEquals(OptionalLong.of(1L), OptionalUtil.mapToLong(Optional.of(1L)));
		Assertions.assertEquals(OptionalDouble.of(1.0), OptionalUtil.mapToDouble(Optional.of(1.0)));

		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.mapToObj((OptionalInt) null));
		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.mapToObj((OptionalLong) null));
		Assertions.assertThrows(NullPointerException.class, () -> OptionalUtil.mapToObj((OptionalDouble) null));
	}

}
