package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FunctionUtilTest {

	@Test
	void truePredicateTest() {
		Assertions.assertSame(FunctionUtil.alwaysTruePredicate(), FunctionUtil.neverFalsePredicate());
		Assertions.assertSame(FunctionUtil.<Integer>alwaysTruePredicate(), FunctionUtil.<String>alwaysTruePredicate());
		Assertions.assertTrue(FunctionUtil.alwaysTruePredicate().test(null));
		Assertions.assertTrue(FunctionUtil.alwaysTruePredicate().test(new Object()));
		Assertions.assertSame(FunctionUtil.<Integer>alwaysTruePredicate().negate(), FunctionUtil.<String>alwaysTruePredicate().negate());
		Assertions.assertSame(FunctionUtil.alwaysFalsePredicate(), FunctionUtil.alwaysTruePredicate().and(FunctionUtil.alwaysFalsePredicate()));
		Assertions.assertSame(FunctionUtil.alwaysTruePredicate(), FunctionUtil.alwaysTruePredicate().or(FunctionUtil.alwaysFalsePredicate()));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.alwaysTruePredicate().and(null));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.alwaysTruePredicate().or(null));
	}

	@Test
	void falsePredicateTest() {
		Assertions.assertSame(FunctionUtil.alwaysFalsePredicate(), FunctionUtil.neverTruePredicate());
		Assertions.assertSame(FunctionUtil.<Integer>alwaysFalsePredicate(), FunctionUtil.<String>alwaysFalsePredicate());
		Assertions.assertFalse(FunctionUtil.alwaysFalsePredicate().test(null));
		Assertions.assertFalse(FunctionUtil.alwaysFalsePredicate().test(new Object()));
		Assertions.assertSame(FunctionUtil.<Integer>alwaysFalsePredicate().negate(), FunctionUtil.<String>alwaysFalsePredicate().negate());
		Assertions.assertSame(FunctionUtil.alwaysFalsePredicate(), FunctionUtil.alwaysFalsePredicate().and(FunctionUtil.alwaysTruePredicate()));
		Assertions.assertSame(FunctionUtil.alwaysTruePredicate(), FunctionUtil.alwaysFalsePredicate().or(FunctionUtil.alwaysTruePredicate()));

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.alwaysFalsePredicate().and(null));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.alwaysFalsePredicate().or(null));
	}

	@Test
	void isNullPredicateTest() {
		Assertions.assertSame(FunctionUtil.<Integer>isNullPredicate(), FunctionUtil.<String>isNullPredicate());
		Assertions.assertTrue(FunctionUtil.isNullPredicate().test(null));
		Assertions.assertFalse(FunctionUtil.isNullPredicate().test(new Object()));
		Assertions.assertSame(FunctionUtil.<Integer>isNullPredicate().negate(), FunctionUtil.<String>isNullPredicate().negate());

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.isNullPredicate().and(null));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.isNullPredicate().or(null));
	}

	@Test
	void nonNullPredicateTest() {
		Assertions.assertSame(FunctionUtil.<Integer>nonNullPredicate(), FunctionUtil.<String>nonNullPredicate());
		Assertions.assertFalse(FunctionUtil.nonNullPredicate().test(null));
		Assertions.assertTrue(FunctionUtil.nonNullPredicate().test(new Object()));
		Assertions.assertSame(FunctionUtil.<Integer>nonNullPredicate().negate(), FunctionUtil.<String>nonNullPredicate().negate());

		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.nonNullPredicate().and(null));
		Assertions.assertThrows(RequirementNotSatisfiedException.class, () -> FunctionUtil.nonNullPredicate().or(null));
	}

}
