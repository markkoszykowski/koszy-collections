package com.koszy.collections;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.agrona.collections.NullReference;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

public class SortedSparseArraySet<K> extends AbstractObjectSortedSet<K> {

	protected Object[] key;

	protected int first = -1;
	protected int last = -1;

	protected int n;
	protected int maxFill;
	protected final int minN;

	protected int size;
	protected final float f;

	protected final Comparator<? super K> comparator;
	protected final RandomGenerator random;

	public SortedSparseArraySet() {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				new Random()
		);
	}

	public SortedSparseArraySet(final int expected) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				new Random()
		);
	}

	public SortedSparseArraySet(final Comparator<? super K> comparator) {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				new Random()
		);
	}

	public SortedSparseArraySet(final RandomGenerator random) {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				random
		);
	}

	public SortedSparseArraySet(
			final int expected,
			final Comparator<? super K> comparator
	) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				new Random()
		);
	}

	public SortedSparseArraySet(
			final int expected,
			final RandomGenerator random
	) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				random
		);
	}

	public SortedSparseArraySet(
			final Comparator<? super K> comparator,
			final RandomGenerator random
	) {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				random
		);
	}

	public SortedSparseArraySet(
			final int expected,
			final Comparator<? super K> comparator,
			final RandomGenerator random
	) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				random
		);
	}

	public SortedSparseArraySet(
			final int expected,
			final float f,
			final Comparator<? super K> comparator,
			final RandomGenerator random
	) {
		if (f <= 0.0 || 1.0 <= f) {
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
		}
		if (expected < 0) {
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		}

		this.f = f;
		this.minN = this.n = HashCommon.arraySize(expected, f);
		this.maxFill = HashCommon.maxFill(this.n, f);
		this.key = new Object[this.n];

		this.comparator = comparator;
		this.random = Objects.requireNonNull(random);
	}


	protected static Object map(final Object k) {
		return k == null ? NullReference.INSTANCE : k;
	}

	@SuppressWarnings(value = {"unchecked"})
	protected static <T> T unmap(final Object k) {
		return k == NullReference.INSTANCE ? null : (T) k;
	}


	@Override
	public Comparator<? super K> comparator() {
		return this.comparator;
	}

	@SuppressWarnings(value = {"unchecked"})
	protected int compare(final K k1, final K k2) {
		return this.comparator == null ? ((Comparable<K>) k1).compareTo(k2) : this.comparator.compare(k1, k2);
	}


	public void ensureCapacity(final int capacity) {
		final int needed = HashCommon.arraySize(capacity, this.f);
		if (this.n < needed) {
			this.resort(needed);
		}
	}

	public boolean trim(final int n) {
		final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / this.f));
		if (this.n <= l || HashCommon.maxFill(l, this.f) < this.size) {
			return true;
		}
		this.resort(l);
		return true;
	}

	public boolean trim() {
		return this.trim(this.size);
	}

	@Override
	public void clear() {
		if (this.size == 0) {
			return;
		}
		this.size = 0;
		Arrays.fill(this.key, null);
		this.first = this.last = -1;
	}


	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}


	@Override
	@SuppressWarnings(value = {"unchecked"})
	public boolean contains(final Object k) {
		final Object[] key = this.key;
		return switch (this.size) {
			case 0 -> false;
			case 1 -> this.compare((K) k, unmap(key[this.first])) == 0;
			case 2 -> this.compare((K) k, unmap(key[this.first])) == 0 || this.compare(unmap(key[this.last]), (K) k) == 0;
			default -> {
				final int first = this.first;
				final int last = this.last;

				int compare;

				compare = this.compare((K) k, unmap(key[first]));
				if (compare < 0) { // k < first
					yield false;
				} else if (compare == 0) { // k == first
					yield true;
				}

				compare = this.compare(unmap(key[last]), (K) k);
				if (compare < 0) { // last < k
					yield false;
				} else if (compare == 0) { // k == last
					yield true;
				}

				final long packed = this.sparseBinarySearch((K) k);
				yield ((int) packed) == ((int) (packed >>> Integer.SIZE));
			}
		};
	}

	@Override
	public boolean add(final K k) {
		final Object[] key = this.key;

		final int begin = 0;
		final int end = this.n - 1;

		return switch (this.size) {
			case 0 -> {
				final int low = begin - 1;
				final int high = end + 1;

				this.first = this.last = this.insert(k, low, high);

				if (this.maxFill <= this.size++) {
					this.resort(HashCommon.arraySize(this.size + 1, this.f));
				}

				yield true;
			}
			case 1 -> {
				final int first = this.first;
				final int last = this.last;

				final int low, high, compare;

				compare = this.compare(k, unmap(key[first]));
				if (compare < 0) { // k < first
					low = begin - 1;
					high = first;
					this.first = this.insert(k, low, high);
					this.last = last < end && key[last + 1] != null ? last + 1 : last;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare > 0) { // last < k
					low = first;
					high = end + 1;
					this.last = this.insert(k, low, high);
					this.first = begin < first && key[first - 1] != null ? first - 1 : first;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else { // k == first == last
					yield false;
				}
			}
			case 2 -> {
				final int first = this.first;
				final int last = this.last;

				final int low, high;

				int compare;

				compare = this.compare(k, unmap(key[first]));
				if (compare < 0) { // k < first
					low = begin - 1;
					high = first;
					this.first = this.insert(k, low, high);
					this.last = last < end && key[last + 1] != null ? last + 1 : last;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare == 0) { // k == first
					yield false;
				}

				compare = this.compare(unmap(key[last]), k);
				if (compare < 0) { // last < k
					low = last;
					high = end + 1;
					this.last = this.insert(k, low, high);
					this.first = begin < first && key[first - 1] != null ? first - 1 : first;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare == 0) { // k == last
					yield false;
				}

				low = first;
				high = last;
				this.insert(k, low, high);
				this.first = begin < first && key[first - 1] != null ? first - 1 : first;
				this.last = last < end && key[last + 1] != null ? last + 1 : last;

				if (this.maxFill <= this.size++) {
					this.resort(HashCommon.arraySize(this.size + 1, this.f));
				}

				yield true;
			}
			default -> {
				final int first = this.first;
				final int last = this.last;

				final int low, high;

				int compare;

				compare = this.compare(k, unmap(key[first]));
				if (compare < 0) { // k < first
					low = begin - 1;
					high = first;
					this.first = this.insert(k, low, high);
					this.last = last < end && key[last + 1] != null ? last + 1 : last;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare == 0) { // k == first
					yield false;
				}

				compare = this.compare(unmap(key[last]), k);
				if (compare < 0) { // last < k
					low = last;
					high = end + 1;
					this.last = this.insert(k, low, high);
					this.first = begin < first && key[first - 1] != null ? first - 1 : first;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare == 0) { // k == last
					yield false;
				}

				final long packed = this.sparseBinarySearch(k);
				low = (int) packed;
				high = (int) (packed >>> Integer.SIZE);
				if (low != high) {
					this.insert(k, low, high);
					this.first = begin < first && key[first - 1] != null ? first - 1 : first;
					this.last = last < end && key[last + 1] != null ? last + 1 : last;

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else {
					yield false;
				}
			}
		};
	}

	private int insert(final K k, final int low, final int high) {
		final Object[] key = this.key;

		assert low < high;

		final int begin = 0;
		final int end = this.n - 1;

		Object last, curr;
		int pos, slot;
		if (high == begin) {
			pos = high;

			slot = pos;
			last = key[slot];

			while (last != null) {
				curr = key[++slot];
				key[slot] = last;
				last = curr;
			}

			key[pos] = map(k);
		} else if (low == end) {
			pos = low;

			slot = pos;
			last = key[slot];

			while (last != null) {
				curr = key[--slot];
				key[slot] = last;
				last = curr;
			}

			key[pos] = map(k);
		} else if (low + 1 == high) { // Need to do some shifting
			if (this.random.nextBoolean()) { // Shift right
				pos = high;

				slot = pos;
				last = key[slot];

				while (last != null && ++slot <= end) {
					curr = key[slot];
					key[slot] = last;
					last = curr;
				}

				if (last == null) {
					key[pos] = map(k);
					return pos;
				}

				// No empty slot, need to shift left

				pos = low;

				while (last != null) {
					curr = key[--slot];
					key[slot] = last;
					last = curr;
				}
			} else { // Shift left
				pos = low;

				slot = pos;
				last = key[slot];

				while (last != null && begin <= --slot) {
					curr = key[slot];
					key[slot] = last;
					last = curr;
				}

				if (last == null) {
					key[pos] = map(k);
					return pos;
				}

				// No empty slot, need to shift right

				pos = high;

				while (last != null) {
					curr = key[++slot];
					key[slot] = last;
					last = curr;
				}
			}

			key[pos] = map(k);
		} else {
			pos = this.random.nextInt(low + 1, high);

			key[pos] = map(k);
		}
		return pos;
	}

	@Override
	@SuppressWarnings(value = {"unchecked"})
	public boolean remove(final Object k) {
		final Object[] key = this.key;
		return switch (this.size) {
			case 0 -> false;
			case 1 -> {
				final int pos = this.first;

				if (this.compare(unmap(key[pos]), (K) k) != 0) {
					yield false;
				}

				this.removeEntry(pos);

				yield true;
			}
			case 2 -> {
				final int first = this.first;
				final int last = this.last;

				int pos, compare;

				pos = first;
				compare = this.compare((K) k, unmap(key[pos]));
				if (compare < 0) { // k < first
					yield false;
				} else if (compare == 0) { // k == first
					this.removeEntry(pos);

					yield true;
				}

				pos = last;
				compare = this.compare(unmap(key[pos]), (K) k);
				if (compare < 0) { // last < k
					yield false;
				} else if (compare == 0) { // k == last
					this.removeEntry(pos);

					yield true;
				}

				yield false;
			}
			default -> {
				final int first = this.first;
				final int last = this.last;

				int pos, compare;

				pos = first;
				compare = this.compare((K) k, unmap(key[pos]));
				if (compare < 0) { // k < first
					yield false;
				} else if (compare == 0) { // k == first
					this.removeEntry(pos);

					yield true;
				}

				pos = last;
				compare = this.compare(unmap(key[pos]), (K) k);
				if (compare < 0) { // last < k
					yield false;
				} else if (compare == 0) { // k == last
					this.removeEntry(pos);

					yield true;
				}

				final long packed = this.sparseBinarySearch((K) k);
				pos = (int) packed;
				if (pos != (int) (packed >>> Integer.SIZE)) {
					yield false;
				}

				this.removeEntry(pos);

				yield true;
			}
		};
	}


	protected void fixPointers(final int i) {
		switch (this.size) {
			case 0 -> this.first = this.last = -1;
			case 1 -> {
				if (i == this.first) {
					this.first = this.last;
				} else if (i == this.last) {
					this.last = this.first;
				}
			}
			default -> {
				final Object[] key = this.key;
				if (i == this.first) {
					while (key[++this.first] == null) {
					}
				} else if (i == this.last) {
					while (key[--this.last] == null) {
					}
				}
			}
		}
	}

	protected void removeEntry(final int pos) {
		--this.size;
		this.fixPointers(pos);
		this.key[pos] = null;
		if (this.minN < this.n && this.size < this.maxFill / 4 && Hash.DEFAULT_INITIAL_SIZE < this.n) {
			this.resort(this.n / 2);
		}
	}


	@Override
	public K first() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		return unmap(this.key[this.first]);
	}

	@Override
	public K last() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		return unmap(this.key[this.last]);
	}

	@Override
	public K removeFirst() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}

		final Object[] key = this.key;
		final int pos = this.first;

		final Object k = key[pos];

		this.removeEntry(pos);

		return unmap(k);
	}

	@Override
	public K removeLast() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}

		final Object[] key = this.key;
		final int pos = this.last;

		final Object k = key[pos];

		this.removeEntry(pos);

		return unmap(k);
	}


	@Override
	public ObjectSortedSet<K> subSet(final K fromElement, final K toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectSortedSet<K> headSet(final K toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectSortedSet<K> tailSet(final K fromElement) {
		throw new UnsupportedOperationException();
	}


	@Override
	public ObjectBidirectionalIterator<K> iterator() {
		return new SetIterator();
	}

	@Override
	public ObjectBidirectionalIterator<K> iterator(final K from) {
		return new SetIterator(from);
	}

	@Override
	public void forEach(final Consumer<? super K> action) {
		final Object[] key = this.key;

		final int last = this.last;

		int curr;
		int next = this.first;
		while (next != -1) {
			curr = next;
			while (++next < last && key[next] == null) {
			}
			if (last < next) {
				next = -1;
			}

			action.accept(unmap(key[curr]));
		}
	}


	protected void resort(final int newN) {
		final Object[] key = this.key;
		final Object[] newKey = new Object[newN];

		final int last = this.last;

		final int size = this.size;
		final RandomGenerator random = this.random;

		int required = size;

		int i = last + 1;
		int j = newN + 1;
		while (0 < required) {
			if (random.nextInt(--j) < required) {
				Object o;
				while ((o = key[--i]) == null) {
				}

				final int pos = j - 1;

				newKey[pos] = o;

				if (required == size) {
					this.last = pos;
				}
				if (--required == 0) {
					this.first = pos;
				}
			}
		}

		this.n = newN;
		this.maxFill = HashCommon.maxFill(this.n, this.f);
		this.key = newKey;
	}

	protected long sparseBinarySearch(final K k) {
		final Object[] key = this.key;

		int low = this.first;
		int high = this.last;

		while (true) {
			int mid = (low + high) >>> 1;

			Object o;
			int sign = -1;
			int distance = 0;
			while ((o = key[mid]) == null) {
				sign = -sign;
				mid += (sign * ++distance);
			}

			if (mid == low || mid == high) {
				return ((high & 0xffffffffL) << Integer.SIZE) | (low & 0xffffffffL);
			}

			assert low < mid && mid < high;
			assert key[low] != null && key[mid] != null && key[high] != null;
			assert this.compare(unmap(key[low]), k) < 0 && this.compare(k, unmap(key[high])) < 0;

			final int compare = this.compare(unmap(o), k);
			if (compare < 0) { // mid < k
				low = mid;
			} else if (compare > 0) { // k < mid
				high = mid;
			} else {
				return ((mid & 0xffffffffL) << Integer.SIZE) | (mid & 0xffffffffL);
			}
		}
	}


	private final class SetIterator implements ObjectBidirectionalIterator<K> {

		int prev = -1;
		int next = -1;
		int curr = -1;

		SetIterator() {
			this.next = SortedSparseArraySet.this.first;
		}

		SetIterator(final K from) {
			final Object[] key = SortedSparseArraySet.this.key;
			switch (SortedSparseArraySet.this.size) {
				case 0 -> throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				case 1 -> {
					final int first = SortedSparseArraySet.this.first;
					final int last = SortedSparseArraySet.this.last;

					final int compare = SortedSparseArraySet.this.compare(from, unmap(key[first]));
					if (compare == 0) { // k == first == last
						this.prev = last;
						return;
					}

					throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				}
				case 2 -> {
					final int first = SortedSparseArraySet.this.first;
					final int last = SortedSparseArraySet.this.last;

					int compare;

					compare = SortedSparseArraySet.this.compare(from, unmap(key[first]));
					if (compare < 0) { // k < first
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == first
						this.prev = first;
						this.next = last;
						return;
					}

					compare = SortedSparseArraySet.this.compare(unmap(key[last]), from);
					if (compare < 0) { // last < k
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == last
						this.prev = last;
						return;
					}

					throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				}
				default -> {
					final int first = SortedSparseArraySet.this.first;
					final int last = SortedSparseArraySet.this.last;

					int compare;

					compare = SortedSparseArraySet.this.compare(from, unmap(key[first]));
					if (compare < 0) { // k < first
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == first
						this.prev = this.next = first;
						while (key[++this.next] == null) {
						}
						return;
					}

					compare = SortedSparseArraySet.this.compare(unmap(key[last]), from);
					if (compare < 0) { // last < k
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == last
						this.prev = last;
						return;
					}

					final long packed = SortedSparseArraySet.this.sparseBinarySearch(from);
					final int pos = (int) packed;
					if (pos == (int) (packed >>> Integer.SIZE)) {
						this.prev = this.next = pos;
						while (++this.next < last && key[this.next] == null) {
						}
						return;
					}

					throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				}
			}
		}

		@Override
		public boolean hasPrevious() {
			return this.prev != -1;
		}

		@Override
		public boolean hasNext() {
			return this.next != -1;
		}

		@Override
		public K previous() {
			if (!this.hasPrevious()) {
				throw new NoSuchElementException();
			}

			final Object[] key = SortedSparseArraySet.this.key;

			final int first = SortedSparseArraySet.this.first;

			this.curr = this.prev;
			while (first < --this.prev && key[this.prev] == null) {
			}
			if (this.prev < first) {
				this.prev = -1;
			}
			this.next = this.curr;

			return unmap(key[this.curr]);
		}

		@Override
		public K next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}

			final Object[] key = SortedSparseArraySet.this.key;

			final int last = SortedSparseArraySet.this.last;

			this.curr = this.next;
			while (++this.next < last && key[this.next] == null) {
			}
			if (last < this.next) {
				this.next = -1;
			}
			this.prev = this.curr;

			return unmap(key[this.curr]);
		}

		@Override
		public void remove() {
			if (this.curr == -1) {
				throw new IllegalStateException();
			}

			final Object[] key = SortedSparseArraySet.this.key;

			final int first = SortedSparseArraySet.this.first;
			final int last = SortedSparseArraySet.this.last;

			final int pos = this.curr;
			if (pos == this.prev) {
				while (first < --this.prev && key[this.prev] == null) {
				}
				if (this.prev < first) {
					this.prev = -1;
				}
			} else if (pos == this.next) {
				while (++this.next < last && key[this.next] == null) {
				}
				if (last < this.next) {
					this.next = -1;
				}
			}

			this.curr = -1;

			--SortedSparseArraySet.this.size;

			if (this.prev == -1) {
				SortedSparseArraySet.this.first = this.next;
			}
			if (this.next == -1) {
				SortedSparseArraySet.this.last = this.prev;
			}

			key[pos] = null;
		}

		@Override
		public void forEachRemaining(final Consumer<? super K> action) {
			final Object[] key = SortedSparseArraySet.this.key;

			final int last = SortedSparseArraySet.this.last;

			while (this.next != -1) {
				this.curr = this.next;
				while (++this.next < last && key[this.next] == null) {
				}
				if (last < this.next) {
					this.next = -1;
				}
				this.prev = this.curr;

				action.accept(unmap(key[this.curr]));
			}
		}
	}

}
