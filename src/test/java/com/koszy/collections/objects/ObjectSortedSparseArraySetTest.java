package com.koszy.collections.objects;

import it.unimi.dsi.fastutil.BidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.agrona.LangUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.random.RandomGenerator;

class ObjectSortedSparseArraySetTest {

	static <T> T previous(final BidirectionalIterator<T> iterator, final int index) {
		T element = null;
		for (int i = 0; i <= index; ++i) {
			element = iterator.previous();
		}
		return element;
	}

	static <T> T next(final Iterator<T> iterator, final int index) {
		T element = null;
		for (int i = 0; i <= index; ++i) {
			element = iterator.next();
		}
		return element;
	}

	static <T> T random(final SortedSet<T> set, final RandomGenerator random) {
		return next(set.iterator(), random.nextInt(set.size()));
	}

	static <T> void assertMayThrow(
			final SortedSet<T> expected,
			final SortedSet<T> actual,
			final Function<? super SortedSet<T>, ? extends T> method,
			final Class<? extends Throwable> throwable
	) {
		final T expectedValue;
		try {
			expectedValue = method.apply(expected);
		} catch (final Throwable expectedThrowable) {
			if (throwable.isInstance(expectedThrowable)) {
				Assertions.assertThrows(throwable, () -> method.apply(actual));
				return;
			} else {
				LangUtil.rethrowUnchecked(expectedThrowable);
				throw new Error();
			}
		}

		Assertions.assertEquals(expectedValue, method.apply(actual));
	}

	static <T> void assertSortedSet(final SortedSet<T> expected, final SortedSet<T> actual) {
		final Iterator<T> expectedIterator = expected.iterator();
		final Iterator<T> actualIterator = actual.iterator();

		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			Assertions.assertEquals(expectedIterator.next(), actualIterator.next());
		}

		Assertions.assertEquals(expectedIterator.hasNext(), actualIterator.hasNext());

		final List<T> expectedList = new ArrayList<>(expected.size());
		final List<T> actualList = new ArrayList<>(actual.size());

		Assertions.assertEquals(expectedList, actualList);
	}

	static void chaos(final Comparator<? super Integer> comparator, final Function<? super RandomGenerator, Integer> generator) {
		final Random random = new Random();

		final SortedSet<Integer> expected = comparator == null ? new TreeSet<>() : new TreeSet<>(comparator);
		final SortedSet<Integer> actual = comparator == null ? new ObjectSortedSparseArraySet<>() : new ObjectSortedSparseArraySet<>(comparator);

		for (int i = 0; i < 1_000_000; ++i) {
			final Integer value = !expected.isEmpty() && random.nextBoolean() ? random(expected, random) : generator.apply(random);
			switch (random.nextInt(7)) {
				case 0 -> Assertions.assertEquals(expected.add(value), actual.add(value));
				case 1 -> Assertions.assertEquals(expected.remove(value), actual.remove(value));
				case 2 -> Assertions.assertEquals(expected.contains(value), actual.contains(value));
				case 3 -> assertMayThrow(expected, actual, SortedSet::first, NoSuchElementException.class);
				case 4 -> assertMayThrow(expected, actual, SortedSet::last, NoSuchElementException.class);
				case 5 -> assertMayThrow(expected, actual, SortedSet::removeFirst, NoSuchElementException.class);
				case 6 -> assertMayThrow(expected, actual, SortedSet::removeLast, NoSuchElementException.class);
				default -> throw new IllegalStateException();
			}

			assertSortedSet(expected, actual);
		}
	}

	@Test
	void chaos() {
		chaos(null, RandomGenerator::nextInt);
	}

	@Test
	void chaosNaturalOrder() {
		chaos(Comparator.naturalOrder(), RandomGenerator::nextInt);
	}

	@Test
	void chaosReverseOrder() {
		chaos(Comparator.reverseOrder(), RandomGenerator::nextInt);
	}

	@Test
	void allowNull() {
		chaos(
				(left, right) -> {
					final Integer l = left == null ? 0 : left;
					final Integer r = right == null ? 0 : right;
					return l.compareTo(r);
				},
				random -> switch (random.nextInt(10)) {
					case 0 -> null;
					default -> {
						final int value = random.nextInt();
						yield value == 0 ? null : value;
					}
				}
		);
	}

	@Test
	void emptyIterator() {
		final ObjectSortedSparseArraySet<Integer> set = new ObjectSortedSparseArraySet<>();

		Assertions.assertThrows(NoSuchElementException.class, () -> set.iterator(0));
		Assertions.assertThrows(NoSuchElementException.class, () -> set.iterator(1));

		final BidirectionalIterator<Integer> iterator = set.iterator();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertFalse(iterator.hasNext());
	}

	@Test
	void oneElementIterator() {
		final ObjectSortedSparseArraySet<Integer> set = new ObjectSortedSparseArraySet<>();

		set.add(1);

		Assertions.assertThrows(NoSuchElementException.class, () -> set.iterator(0));
		Assertions.assertThrows(NoSuchElementException.class, () -> set.iterator(2));

		final BidirectionalIterator<Integer> iterator = set.iterator();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals(1, iterator.next());

		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertTrue(iterator.hasPrevious());
		Assertions.assertEquals(1, iterator.previous());

		final BidirectionalIterator<Integer> fromIterator = set.iterator(1);

		Assertions.assertFalse(fromIterator.hasNext());
		Assertions.assertTrue(fromIterator.hasPrevious());
		Assertions.assertEquals(1, fromIterator.previous());

		Assertions.assertFalse(fromIterator.hasPrevious());
		Assertions.assertTrue(fromIterator.hasNext());
		Assertions.assertEquals(1, fromIterator.next());
	}

	@Test
	void twoElementIterator() {
		final ObjectSortedSparseArraySet<Integer> set = new ObjectSortedSparseArraySet<>();

		set.add(1);
		set.add(2);

		Assertions.assertThrows(NoSuchElementException.class, () -> set.iterator(0));
		Assertions.assertThrows(NoSuchElementException.class, () -> set.iterator(3));

		final BidirectionalIterator<Integer> iterator = set.iterator();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals(1, iterator.next());

		Assertions.assertTrue(iterator.hasPrevious());
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals(2, iterator.next());

		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertTrue(iterator.hasPrevious());
		Assertions.assertEquals(2, iterator.previous());

		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertTrue(iterator.hasPrevious());
		Assertions.assertEquals(1, iterator.previous());

		final BidirectionalIterator<Integer> fromIterator1 = set.iterator(1);

		Assertions.assertTrue(fromIterator1.hasPrevious());
		Assertions.assertTrue(fromIterator1.hasNext());
		Assertions.assertEquals(2, fromIterator1.next());

		Assertions.assertFalse(fromIterator1.hasNext());
		Assertions.assertTrue(fromIterator1.hasPrevious());
		Assertions.assertEquals(2, fromIterator1.previous());

		Assertions.assertTrue(fromIterator1.hasNext());
		Assertions.assertTrue(fromIterator1.hasPrevious());
		Assertions.assertEquals(1, fromIterator1.previous());

		Assertions.assertFalse(fromIterator1.hasPrevious());
		Assertions.assertTrue(fromIterator1.hasNext());
		Assertions.assertEquals(1, fromIterator1.next());

		final BidirectionalIterator<Integer> fromIterator2 = set.iterator(2);

		Assertions.assertFalse(fromIterator2.hasNext());
		Assertions.assertTrue(fromIterator2.hasPrevious());
		Assertions.assertEquals(2, fromIterator2.previous());

		Assertions.assertTrue(fromIterator2.hasNext());
		Assertions.assertTrue(fromIterator2.hasPrevious());
		Assertions.assertEquals(1, fromIterator2.previous());

		Assertions.assertFalse(fromIterator2.hasPrevious());
		Assertions.assertTrue(fromIterator2.hasNext());
		Assertions.assertEquals(1, fromIterator2.next());

		Assertions.assertTrue(fromIterator2.hasPrevious());
		Assertions.assertTrue(fromIterator2.hasNext());
		Assertions.assertEquals(2, fromIterator2.next());
	}

	@Test
	void oneElementIteratorRemove() {
		final ObjectSortedSparseArraySet<Integer> set = new ObjectSortedSparseArraySet<>();

		set.add(1);

		final BidirectionalIterator<Integer> iterator = set.iterator();

		iterator.next();
		iterator.remove();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertTrue(set.isEmpty());
	}

	@Test
	void twoElementIteratorRemove() {
		final ObjectSortedSparseArraySet<Integer> set = new ObjectSortedSparseArraySet<>();

		set.add(1);
		set.add(2);

		final BidirectionalIterator<Integer> iterator = set.iterator();

		iterator.next();
		iterator.remove();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals(1, set.size());

		iterator.next();
		iterator.remove();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertTrue(set.isEmpty());
	}

	@Test
	void threeElementIteratorRemove() {
		final ObjectSortedSparseArraySet<Integer> set = new ObjectSortedSparseArraySet<>();

		set.add(1);
		set.add(2);
		set.add(3);

		final BidirectionalIterator<Integer> iterator = set.iterator();

		iterator.next();
		iterator.remove();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals(2, set.size());

		iterator.next();
		iterator.remove();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals(1, set.size());

		iterator.next();
		iterator.remove();

		Assertions.assertFalse(iterator.hasPrevious());
		Assertions.assertFalse(iterator.hasNext());
		Assertions.assertTrue(set.isEmpty());
	}

	static <T> void assertIteratorPrevious(final BidirectionalIterator<T> expected, final BidirectionalIterator<T> actual) {
		while (expected.hasPrevious() && actual.hasPrevious()) {
			Assertions.assertEquals(expected.previous(), actual.previous());
		}
		Assertions.assertEquals(expected.hasPrevious(), actual.hasPrevious());
	}

	static <T> void assertIteratorNext(final BidirectionalIterator<T> expected, final BidirectionalIterator<T> actual) {
		while (expected.hasNext() && actual.hasNext()) {
			Assertions.assertEquals(expected.next(), actual.next());
		}
		Assertions.assertEquals(expected.next(), actual.next());
	}

	static void iteratorChaos(final Comparator<? super Integer> comparator) {
		final Random random = new Random();

		final ObjectSortedSet<Integer> expected = comparator == null ? new ObjectAVLTreeSet<>() : new ObjectAVLTreeSet<>(comparator);
		final ObjectSortedSet<Integer> actual = comparator == null ? new ObjectSortedSparseArraySet<>() : new ObjectSortedSparseArraySet<>(comparator);

		for (int i = 0; i < 1_000; ++i) {
			final int value = random.nextInt();
			Assertions.assertEquals(expected.add(value), actual.add(value));
		}

		for (int i = 0; i < 1_000; ++i) {
			final BidirectionalIterator<Integer> expectedIterator;
			final BidirectionalIterator<Integer> actualIterator;

			if (random.nextBoolean()) {
				expectedIterator = expected.iterator();
				actualIterator = actual.iterator();

				final int next = random.nextInt(expected.size());
				Assertions.assertEquals(next(expectedIterator, next), next(actualIterator, next));
			} else {
				final int from = random.nextInt(expected.size());
				final Integer value = next(expected.iterator(), from);

				expectedIterator = expected.iterator(value);
				actualIterator = actual.iterator(value);

				if (from == expected.size() - 1 || random.nextBoolean()) {
					final int previous = random.nextInt(from + 1);
					Assertions.assertEquals(previous(expectedIterator, previous), previous(actualIterator, previous));
				} else {
					final int next = random.nextInt(expected.size() - from - 1);
					Assertions.assertEquals(next(expectedIterator, next), next(actualIterator, next));
				}
			}

			if (random.nextBoolean()) {
				expectedIterator.remove();
				actualIterator.remove();
			}

			if (random.nextBoolean()) {
				assertIteratorPrevious(expectedIterator, actualIterator);
			} else {
				assertIteratorPrevious(expectedIterator, actualIterator);
			}
		}
	}

	@Test
	void iteratorChaos() {
		iteratorChaos(null);
	}

	@Test
	void iteratorChaosNaturalOrder() {
		iteratorChaos(Comparator.naturalOrder());
	}

	@Test
	void iteratorChaosReverseOrder() {
		iteratorChaos(Comparator.reverseOrder());
	}

}
