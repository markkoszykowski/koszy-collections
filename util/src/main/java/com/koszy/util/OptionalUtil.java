package com.koszy.util;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;

public final class OptionalUtil {


	public static OptionalInt ofNullable(final Integer i) {
		return i == null ? OptionalInt.empty() : OptionalInt.of(i);
	}

	public static OptionalInt ofNullable(final int i) {
		return OptionalInt.of(i);
	}

	public static OptionalLong ofNullable(final Long l) {
		return l == null ? OptionalLong.empty() : OptionalLong.of(l);
	}

	public static OptionalLong ofNullable(final long l) {
		return OptionalLong.of(l);
	}

	public static OptionalDouble ofNullable(final Double d) {
		return d == null ? OptionalDouble.empty() : OptionalDouble.of(d);
	}

	public static OptionalDouble ofNullable(final double d) {
		return OptionalDouble.of(d);
	}


	public static Integer getNullable(final OptionalInt optional) {
		return optional.isPresent() ? optional.getAsInt() : null;
	}

	public static Long getNullable(final OptionalLong optional) {
		return optional.isPresent() ? optional.getAsLong() : null;
	}

	public static Double getNullable(final OptionalDouble optional) {
		return optional.isPresent() ? optional.getAsDouble() : null;
	}


	/// @see java.util.stream.IntStream#mapToObj(IntFunction)
	public static Optional<Integer> mapToObj(final OptionalInt optional) {
		return optional.isPresent() ? Optional.of(optional.getAsInt()) : Optional.empty();
	}

	/// @see java.util.stream.LongStream#mapToObj(LongFunction)
	public static Optional<Long> mapToObj(final OptionalLong optional) {
		return optional.isPresent() ? Optional.of(optional.getAsLong()) : Optional.empty();
	}

	/// @see java.util.stream.DoubleStream#mapToObj(DoubleFunction)
	public static Optional<Double> mapToObj(final OptionalDouble optional) {
		return optional.isPresent() ? Optional.of(optional.getAsDouble()) : Optional.empty();
	}


	/// @see java.util.stream.Stream#mapToInt(java.util.function.ToIntFunction)
	public static OptionalInt mapToInt(final Optional<Integer> optional) {
		return optional.isPresent() ? OptionalInt.of(optional.get()) : OptionalInt.empty();
	}

	/// @see java.util.stream.Stream#mapToLong(java.util.function.ToLongFunction)
	public static OptionalLong mapToLong(final Optional<Long> optional) {
		return optional.isPresent() ? OptionalLong.of(optional.get()) : OptionalLong.empty();
	}

	/// @see java.util.stream.Stream#mapToDouble(java.util.function.ToDoubleFunction)
	public static OptionalDouble mapToDouble(final Optional<Double> optional) {
		return optional.isPresent() ? OptionalDouble.of(optional.get()) : OptionalDouble.empty();
	}


	/// @see Optional#filter(Predicate)
	public static OptionalInt filter(final OptionalInt optional, final IntPredicate filter) {
		Require.notNull(filter);

		final int value;
		return optional.isPresent() && filter.test(value = optional.getAsInt()) ? OptionalInt.of(value) : OptionalInt.empty();
	}

	/// @see Optional#filter(Predicate)
	public static OptionalLong filter(final OptionalLong optional, final LongPredicate filter) {
		Require.notNull(filter);

		final long value;
		return optional.isPresent() && filter.test(value = optional.getAsLong()) ? OptionalLong.of(value) : OptionalLong.empty();
	}

	/// @see Optional#filter(Predicate)
	public static OptionalDouble filter(final OptionalDouble optional, final DoublePredicate filter) {
		Require.notNull(filter);

		final double value;
		return optional.isPresent() && filter.test(value = optional.getAsDouble()) ? OptionalDouble.of(value) : OptionalDouble.empty();
	}


	/// @see Optional#filter(Predicate)
	public static OptionalInt filter(final OptionalInt optional, final Predicate<? super Integer> filter) {
		Require.notNull(filter);

		final int value;
		return optional.isPresent() && filter.test(value = optional.getAsInt()) ? OptionalInt.of(value) : OptionalInt.empty();
	}

	/// @see Optional#filter(Predicate)
	public static OptionalLong filter(final OptionalLong optional, final Predicate<? super Long> filter) {
		Require.notNull(filter);

		final long value;
		return optional.isPresent() && filter.test(value = optional.getAsLong()) ? OptionalLong.of(value) : OptionalLong.empty();
	}

	/// @see Optional#filter(Predicate)
	public static OptionalDouble filter(final OptionalDouble optional, final Predicate<? super Double> filter) {
		Require.notNull(filter);

		final double value;
		return optional.isPresent() && filter.test(value = optional.getAsDouble()) ? OptionalDouble.of(value) : OptionalDouble.empty();
	}


	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.IntStream#map(IntUnaryOperator)
	public static OptionalInt map(final OptionalInt optional, final IntUnaryOperator map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalInt.of(map.applyAsInt(optional.getAsInt())) : OptionalInt.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.LongStream#mapToInt(LongToIntFunction)
	public static OptionalInt mapToInt(final OptionalLong optional, final LongToIntFunction map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalInt.of(map.applyAsInt(optional.getAsLong())) : OptionalInt.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.DoubleStream#mapToInt(DoubleToIntFunction)
	public static OptionalInt mapToInt(final OptionalDouble optional, final DoubleToIntFunction map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalInt.of(map.applyAsInt(optional.getAsDouble())) : OptionalInt.empty();
	}


	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.IntStream#mapToLong(IntToLongFunction)
	public static OptionalLong mapToLong(final OptionalInt optional, final IntToLongFunction map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalLong.of(map.applyAsLong(optional.getAsInt())) : OptionalLong.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.LongStream#map(LongUnaryOperator)
	public static OptionalLong map(final OptionalLong optional, final LongUnaryOperator map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalLong.of(map.applyAsLong(optional.getAsLong())) : OptionalLong.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.DoubleStream#mapToLong(DoubleToLongFunction)
	public static OptionalLong mapToLong(final OptionalDouble optional, final DoubleToLongFunction map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalLong.of(map.applyAsLong(optional.getAsDouble())) : OptionalLong.empty();
	}


	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.IntStream#mapToDouble(IntToDoubleFunction)
	public static OptionalDouble mapToDouble(final OptionalInt optional, final IntToDoubleFunction map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalDouble.of(map.applyAsDouble(optional.getAsInt())) : OptionalDouble.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.LongStream#mapToDouble(LongToDoubleFunction)
	public static OptionalDouble mapToDouble(final OptionalLong optional, final LongToDoubleFunction map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalDouble.of(map.applyAsDouble(optional.getAsLong())) : OptionalDouble.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.DoubleStream#map(DoubleUnaryOperator)
	public static OptionalDouble map(final OptionalDouble optional, final DoubleUnaryOperator map) {
		Require.notNull(map);
		return optional.isPresent() ? OptionalDouble.of(map.applyAsDouble(optional.getAsDouble())) : OptionalDouble.empty();
	}


	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.IntStream#mapToObj(IntFunction)
	public static <T> Optional<T> mapToObj(final OptionalInt optional, final IntFunction<? extends T> map) {
		Require.notNull(map);
		return optional.isPresent() ? Optional.ofNullable(map.apply(optional.getAsInt())) : Optional.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.LongStream#mapToObj(LongFunction)
	public static <T> Optional<T> mapToObj(final OptionalLong optional, final LongFunction<? extends T> map) {
		Require.notNull(map);
		return optional.isPresent() ? Optional.ofNullable(map.apply(optional.getAsLong())) : Optional.empty();
	}

	/// @see Optional#map(java.util.function.Function)
	/// @see java.util.stream.DoubleStream#mapToObj(DoubleFunction)
	public static <T> Optional<T> mapToObj(final OptionalDouble optional, final DoubleFunction<? extends T> map) {
		Require.notNull(map);
		return optional.isPresent() ? Optional.ofNullable(map.apply(optional.getAsDouble())) : Optional.empty();
	}


	/// @see Optional#flatMap(java.util.function.Function)
	/// @see java.util.stream.IntStream#flatMap(IntFunction)
	public static OptionalInt flatMap(final OptionalInt optional, final IntFunction<? extends OptionalInt> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsInt())) : OptionalInt.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	public static OptionalInt flatMapToInt(final OptionalLong optional, final LongFunction<? extends OptionalInt> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsLong())) : OptionalInt.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	public static OptionalInt flatMapToInt(final OptionalDouble optional, final DoubleFunction<? extends OptionalInt> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsDouble())) : OptionalInt.empty();
	}


	/// @see Optional#flatMap(java.util.function.Function)
	public static OptionalLong flatMapToLong(final OptionalInt optional, final IntFunction<? extends OptionalLong> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsInt())) : OptionalLong.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	/// @see java.util.stream.LongStream#flatMap(LongFunction)
	public static OptionalLong flatMap(final OptionalLong optional, final LongFunction<? extends OptionalLong> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsLong())) : OptionalLong.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	public static OptionalLong flatMapToLong(final OptionalDouble optional, final DoubleFunction<? extends OptionalLong> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsDouble())) : OptionalLong.empty();
	}


	/// @see Optional#flatMap(java.util.function.Function)
	public static OptionalDouble flatMapToDouble(final OptionalInt optional, final IntFunction<? extends OptionalDouble> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsInt())) : OptionalDouble.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	public static OptionalDouble flatMapToDouble(final OptionalLong optional, final LongFunction<? extends OptionalDouble> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsLong())) : OptionalDouble.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	/// @see java.util.stream.DoubleStream#flatMap(DoubleFunction)
	public static OptionalDouble flatMap(final OptionalDouble optional, final DoubleFunction<? extends OptionalDouble> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsDouble())) : OptionalDouble.empty();
	}


	/// @see Optional#flatMap(java.util.function.Function)
	public static <T> Optional<T> flatMapToObj(final OptionalInt optional, final IntFunction<? extends Optional<T>> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsInt())) : Optional.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	public static <T> Optional<T> flatMapToObj(final OptionalLong optional, final LongFunction<? extends Optional<T>> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsLong())) : Optional.empty();
	}

	/// @see Optional#flatMap(java.util.function.Function)
	public static <T> Optional<T> flatMapToObj(final OptionalDouble optional, final DoubleFunction<? extends Optional<T>> map) {
		Require.notNull(map);
		return optional.isPresent() ? Require.notNull(map.apply(optional.getAsDouble())) : Optional.empty();
	}


	private OptionalUtil() {
		LangUtil.unreachable();
	}

}
