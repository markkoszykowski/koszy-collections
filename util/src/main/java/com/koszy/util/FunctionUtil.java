package com.koszy.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class FunctionUtil {

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Predicate<T> alwaysTruePredicate() {
		return (Predicate<T>) TruePredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Predicate<T> alwaysFalsePredicate() {
		return (Predicate<T>) FalsePredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Predicate<T> neverTruePredicate() {
		return (Predicate<T>) FalsePredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Predicate<T> neverFalsePredicate() {
		return (Predicate<T>) TruePredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, U> BiPredicate<T, U> alwaysTrueBiPredicate() {
		return (BiPredicate<T, U>) TrueBiPredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, U> BiPredicate<T, U> alwaysFalseBiPredicate() {
		return (BiPredicate<T, U>) FalseBiPredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, U> BiPredicate<T, U> neverTrueBiPredicate() {
		return (BiPredicate<T, U>) FalseBiPredicate.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, U> BiPredicate<T, U> neverFalseBiPredicate() {
		return (BiPredicate<T, U>) TrueBiPredicate.INSTANCE;
	}


	public static IntConsumer noOpIntConsumer() {
		return NoOpConsumer.INSTANCE;
	}

	public static LongConsumer noOpLongConsumer() {
		return NoOpConsumer.INSTANCE;
	}

	public static DoubleConsumer noOpDoubleConsumer() {
		return NoOpConsumer.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Consumer<T> noOpConsumer() {
		return (Consumer<T>) NoOpConsumer.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> ObjIntConsumer<T> noOpObjIntConsumer() {
		return (ObjIntConsumer<T>) NoOpBiConsumer.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> ObjLongConsumer<T> noOpObjLongConsumer() {
		return (ObjLongConsumer<T>) NoOpBiConsumer.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> ObjDoubleConsumer<T> noOpObjDoubleConsumer() {
		return (ObjDoubleConsumer<T>) NoOpBiConsumer.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, U> BiConsumer<T, U> noOpBiConsumer() {
		return (BiConsumer<T, U>) NoOpBiConsumer.INSTANCE;
	}


	public static BooleanSupplier alwaysTrueSupplier() {
		return TrueSupplier.INSTANCE;
	}

	public static BooleanSupplier alwaysFalseSupplier() {
		return FalseSupplier.INSTANCE;
	}

	public static BooleanSupplier neverTrueSupplier() {
		return FalseSupplier.INSTANCE;
	}

	public static BooleanSupplier neverFalseSupplier() {
		return TrueSupplier.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> Supplier<T> nullSupplier() {
		return (Supplier<T>) NullSupplier.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, R> Function<T, R> nullFunction() {
		return (Function<T, R>) NullFunction.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> UnaryOperator<T> nullUnaryOperator() {
		return (UnaryOperator<T>) NullFunction.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T, U, R> BiFunction<T, U, R> nullBiFunction() {
		return (BiFunction<T, U, R>) NullBiFunction.INSTANCE;
	}

	@SuppressWarnings(value = {"unchecked"})
	public static <T> BinaryOperator<T> nullBinaryOperator() {
		return (BinaryOperator<T>) NullBiFunction.INSTANCE;
	}


	private enum TruePredicate implements Predicate<Object> {
		INSTANCE;

		@Override
		public boolean test(final Object object) {
			return true;
		}

		@Override
		public Predicate<Object> and(final Predicate<? super Object> other) {
			Require.notNull(other);
			return other;
		}

		@Override
		public Predicate<Object> or(final Predicate<? super Object> other) {
			Require.notNull(other);
			return this;
		}

		@Override
		public Predicate<Object> negate() {
			return FalsePredicate.INSTANCE;
		}
	}

	private enum FalsePredicate implements Predicate<Object> {
		INSTANCE;

		@Override
		public boolean test(final Object object) {
			return false;
		}

		@Override
		public Predicate<Object> and(final Predicate<? super Object> other) {
			Require.notNull(other);
			return this;
		}

		@Override
		public Predicate<Object> or(final Predicate<? super Object> other) {
			Require.notNull(other);
			return other;
		}

		@Override
		public Predicate<Object> negate() {
			return TruePredicate.INSTANCE;
		}
	}

	private enum TrueBiPredicate implements BiPredicate<Object, Object> {
		INSTANCE;

		@Override
		public boolean test(final Object object1, final Object object2) {
			return true;
		}

		@Override
		public BiPredicate<Object, Object> and(final BiPredicate<? super Object, ? super Object> other) {
			Require.notNull(other);
			return other;
		}

		@Override
		public BiPredicate<Object, Object> or(final BiPredicate<? super Object, ? super Object> other) {
			Require.notNull(other);
			return this;
		}

		@Override
		public BiPredicate<Object, Object> negate() {
			return FalseBiPredicate.INSTANCE;
		}
	}

	private enum FalseBiPredicate implements BiPredicate<Object, Object> {
		INSTANCE;

		@Override
		public boolean test(final Object object1, final Object object2) {
			return false;
		}

		@Override
		public BiPredicate<Object, Object> and(final BiPredicate<? super Object, ? super Object> other) {
			Require.notNull(other);
			return this;
		}

		@Override
		public BiPredicate<Object, Object> or(final BiPredicate<? super Object, ? super Object> other) {
			Require.notNull(other);
			return other;
		}

		@Override
		public BiPredicate<Object, Object> negate() {
			return TrueBiPredicate.INSTANCE;
		}
	}

	private enum NoOpConsumer implements IntConsumer, LongConsumer, DoubleConsumer, Consumer<Object> {
		INSTANCE;

		@Override
		public void accept(final int value) {
		}

		@Override
		public void accept(final long value) {
		}

		@Override
		public void accept(final double value) {
		}

		@Override
		public void accept(final Object object) {
		}

		@Override
		public IntConsumer andThen(final IntConsumer after) {
			Require.notNull(after);
			return after;
		}

		@Override
		public LongConsumer andThen(final LongConsumer after) {
			Require.notNull(after);
			return after;
		}

		@Override
		public DoubleConsumer andThen(final DoubleConsumer after) {
			Require.notNull(after);
			return after;
		}

		@Override
		public Consumer<Object> andThen(final Consumer<? super Object> after) {
			Require.notNull(after);
			return after;
		}
	}

	private enum NoOpBiConsumer implements ObjIntConsumer<Object>, ObjLongConsumer<Object>, ObjDoubleConsumer<Object>, BiConsumer<Object, Object> {
		INSTANCE;

		@Override
		public void accept(final Object object, final int value) {
		}

		@Override
		public void accept(final Object object, final long value) {
		}

		@Override
		public void accept(final Object object, final double value) {
		}

		@Override
		public void accept(final Object object1, final Object object2) {
		}

		@Override
		public BiConsumer<Object, Object> andThen(final BiConsumer<? super Object, ? super Object> after) {
			Require.notNull(after);
			return after;
		}
	}

	private enum TrueSupplier implements BooleanSupplier {
		INSTANCE;

		@Override
		public boolean getAsBoolean() {
			return true;
		}
	}

	private enum FalseSupplier implements BooleanSupplier {
		INSTANCE;

		@Override
		public boolean getAsBoolean() {
			return false;
		}
	}

	private enum NullSupplier implements Supplier<Object> {
		INSTANCE;

		@Override
		public Object get() {
			return null;
		}
	}

	private enum NullFunction implements Function<Object, Object>, UnaryOperator<Object> {
		INSTANCE;

		@Override
		public Object apply(final Object object) {
			return null;
		}

		@Override
		@SuppressWarnings(value = {"unchecked"})
		public <V> Function<V, Object> compose(final Function<? super V, ?> before) {
			Require.notNull(before);
			return (Function<V, Object>) this;
		}

		@Override
		public <V> Function<Object, V> andThen(final Function<? super Object, ? extends V> after) {
			Require.notNull(after);
			return t -> after.apply(null);
		}
	}

	private enum NullBiFunction implements BiFunction<Object, Object, Object>, BinaryOperator<Object> {
		INSTANCE;

		@Override
		public Object apply(final Object object1, final Object object2) {
			return null;
		}

		@Override
		public <V> BiFunction<Object, Object, V> andThen(final Function<? super Object, ? extends V> after) {
			Require.notNull(after);
			return (t, u) -> after.apply(null);
		}
	}

	private FunctionUtil() {
		LangUtil.unreachable();
	}

}
