package com.koszy.collections.chars;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.chars.AbstractCharSortedSet;
import it.unimi.dsi.fastutil.chars.CharBidirectionalIterator;
import it.unimi.dsi.fastutil.chars.CharComparator;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharSortedSet;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

public class CharSortedSparseArraySet extends AbstractCharSortedSet {

	protected char[] key;

	protected int nulll = -1;
	protected int first = -1;
	protected int last = -1;

	protected int n;
	protected int maxFill;
	protected final int minN;

	protected int size;
	protected final float f;

	protected final CharComparator comparator;
	protected final RandomGenerator random;

	public CharSortedSparseArraySet() {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				new Random()
		);
	}

	public CharSortedSparseArraySet(final int expected) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				new Random()
		);
	}

	public CharSortedSparseArraySet(final CharComparator comparator) {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				new Random()
		);
	}

	public CharSortedSparseArraySet(final RandomGenerator random) {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				null,
				random
		);
	}

	public CharSortedSparseArraySet(
			final int expected,
			final CharComparator comparator
	) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				new Random()
		);
	}

	public CharSortedSparseArraySet(
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

	public CharSortedSparseArraySet(
			final CharComparator comparator,
			final RandomGenerator random
	) {
		this(
				Hash.DEFAULT_INITIAL_SIZE,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				random
		);
	}

	public CharSortedSparseArraySet(
			final int expected,
			final CharComparator comparator,
			final RandomGenerator random
	) {
		this(
				expected,
				Hash.DEFAULT_LOAD_FACTOR,
				comparator,
				random
		);
	}

	public CharSortedSparseArraySet(
			final int expected,
			final float f,
			final CharComparator comparator,
			final RandomGenerator random
	) {
		if (f <= 0.0F || 1.0F <= f) {
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
		}
		if (expected < 0) {
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		}

		this.f = f;
		this.minN = this.n = HashCommon.arraySize(expected, f);
		this.maxFill = HashCommon.maxFill(this.n, f);
		this.key = new char[this.n];

		this.comparator = comparator;
		this.random = Objects.requireNonNull(random);
	}


	@Override
	public CharComparator comparator() {
		return this.comparator;
	}

	protected int compare(final char k1, final char k2) {
		return this.comparator == null ? Character.compare(k1, k2) : this.comparator.compare(k1, k2);
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
		try {
			this.resort(l);
		} catch (final OutOfMemoryError cantDoIt) {
			return false;
		}
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
		Arrays.fill(this.key, (char) 0);
		this.nulll = this.first = this.last = -1;
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
	public boolean contains(final char k) {
		final char[] key = this.key;
		return switch (this.size) {
			case 0 -> false;
			case 1 -> this.compare(k, key[this.first]) == 0;
			case 2 -> this.compare(k, key[this.first]) == 0 || this.compare(key[this.last], k) == 0;
			default -> {
				final int first = this.first;
				final int last = this.last;

				int compare;

				compare = this.compare(k, key[first]);
				if (compare < 0) { // k < first
					yield false;
				} else if (compare == 0) { // k == first
					yield true;
				}

				compare = this.compare(key[last], k);
				if (compare < 0) { // last < k
					yield false;
				} else if (compare == 0) { // k == last
					yield true;
				}

				final long packed = this.sparseBinarySearch(k);
				yield ((int) packed) == ((int) (packed >>> Integer.SIZE));
			}
		};
	}

	@Override
	public boolean add(final char k) {
		final char[] key = this.key;

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

				compare = this.compare(k, key[first]);
				if (compare < 0) { // k < first
					low = begin - 1;
					high = first;
					this.first = this.insert(k, low, high);

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare > 0) { // last < k
					low = last;
					high = end + 1;
					this.last = this.insert(k, low, high);

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

				compare = this.compare(k, key[first]);
				if (compare < 0) { // k < first
					low = begin - 1;
					high = first;
					this.first = this.insert(k, low, high);

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare == 0) { // k == first
					yield false;
				}

				compare = this.compare(key[last], k);
				if (compare < 0) { // last < k
					low = last;
					high = end + 1;
					this.last = this.insert(k, low, high);

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

				compare = this.compare(k, key[first]);
				if (compare < 0) { // k < first
					low = begin - 1;
					high = first;
					this.first = this.insert(k, low, high);

					if (this.maxFill <= this.size++) {
						this.resort(HashCommon.arraySize(this.size + 1, this.f));
					}

					yield true;
				} else if (compare == 0) { // k == first
					yield false;
				}

				compare = this.compare(key[last], k);
				if (compare < 0) { // last < k
					low = last;
					high = end + 1;
					this.last = this.insert(k, low, high);

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

	private int insert(final char k, final int low, final int high) {
		final char[] key = this.key;

		assert low < high;

		final int begin = 0;
		final int end = this.n - 1;

		char last, curr;
		int pos, slot, nulll;
		if (high == begin) {
			last = k;

			pos = high;

			slot = pos - 1;
			nulll = last == (char) 0 ? slot : this.nulll;

			while (last != (char) 0 || slot == nulll) {
				curr = key[++slot];
				key[slot] = last;
				if (last == (char) 0) {
					this.nulll = slot;
				}
				last = curr;
			}

			this.last = Math.max(slot, this.last);
		} else if (low == end) {
			last = k;

			pos = low;

			slot = pos + 1;
			nulll = last == (char) 0 ? slot : this.nulll;

			while (last != (char) 0 || slot == nulll) {
				curr = key[--slot];
				key[slot] = last;
				if (last == (char) 0) {
					this.nulll = slot;
				}
				last = curr;
			}

			this.first = Math.min(slot, this.first);
		} else if (low + 1 == high) { // Need to do some shifting
			if (this.random.nextBoolean()) { // Shift right
				last = k;

				pos = high;

				slot = pos - 1;
				nulll = last == (char) 0 ? slot : this.nulll;

				while (slot < end && (last != (char) 0 || slot == nulll)) {
					curr = key[++slot];
					key[slot] = last;
					if (last == (char) 0) {
						this.nulll = slot;
					}
					last = curr;
				}

				if (last == (char) 0 && slot != nulll) {
					this.last = Math.max(slot, this.last);

					return pos;
				}

				// No empty slot, need to shift left

				pos = low;

				slot = slot + 1;
				nulll = last == (char) 0 ? slot : this.nulll;

				while (last != (char) 0 || slot == nulll) {
					curr = key[--slot];
					key[slot] = last;
					if (last == (char) 0) {
						this.nulll = slot;
					}
					last = curr;
				}

				this.first = Math.min(slot, this.first);
			} else { // Shift left
				last = k;

				pos = low;

				slot = pos + 1;
				nulll = last == (char) 0 ? slot : this.nulll;

				while (begin < slot && (last != (char) 0 || slot == nulll)) {
					curr = key[--slot];
					key[slot] = last;
					if (last == (char) 0) {
						this.nulll = slot;
					}
					last = curr;
				}

				if (last == (char) 0 && slot != nulll) {
					this.first = Math.min(slot, this.first);

					return pos;
				}

				// No empty slot, need to shift right

				pos = high;

				slot = slot - 1;
				nulll = last == (char) 0 ? slot : this.nulll;

				while (last != (char) 0 || slot == nulll) {
					curr = key[++slot];
					key[slot] = last;
					if (last == (char) 0) {
						this.nulll = slot;
					}
					last = curr;
				}

				this.last = Math.max(slot, this.last);
			}
		} else {
			pos = this.random.nextInt(low + 1, high);

			key[pos] = k;
			if (k == (char) 0) {
				this.nulll = pos;
			}
		}

		return pos;
	}

	@Override
	public boolean remove(final char k) {
		final char[] key = this.key;
		return switch (this.size) {
			case 0 -> false;
			case 1 -> {
				final int pos = this.first;

				if (this.compare(key[pos], k) != 0) {
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
				compare = this.compare(k, key[pos]);
				if (compare < 0) { // k < first
					yield false;
				} else if (compare == 0) { // k == first
					this.removeEntry(pos);

					yield true;
				}

				pos = last;
				compare = this.compare(key[pos], k);
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
				compare = this.compare(k, key[pos]);
				if (compare < 0) { // k < first
					yield false;
				} else if (compare == 0) { // k == first
					this.removeEntry(pos);

					yield true;
				}

				pos = last;
				compare = this.compare(key[pos], k);
				if (compare < 0) { // last < k
					yield false;
				} else if (compare == 0) { // k == last
					this.removeEntry(pos);

					yield true;
				}

				final long packed = this.sparseBinarySearch(k);
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
			case 0 -> this.nulll = this.first = this.last = -1;
			case 1 -> {
				if (i == this.nulll) {
					this.nulll = -1;
				}

				if (i == this.first) {
					this.first = this.last;
				} else if (i == this.last) {
					this.last = this.first;
				}
			}
			default -> {
				final char[] key = this.key;

				final int nulll = this.nulll;

				if (i == nulll) {
					this.nulll = -1;
				}

				if (i == this.first) {
					while (key[++this.first] == (char) 0 && this.first != nulll) {
					}
				} else if (i == this.last) {
					while (key[--this.last] == (char) 0 && this.last != nulll) {
					}
				}
			}
		}
	}

	protected void removeEntry(final int pos) {
		--this.size;
		this.fixPointers(pos);
		this.key[pos] = (char) 0;
		if (this.minN < this.n && this.size < this.maxFill / 4 && Hash.DEFAULT_INITIAL_SIZE < this.n) {
			this.resort(this.n / 2);
		}
	}


	@Override
	public char firstChar() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		return this.key[this.first];
	}

	@Override
	public char lastChar() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		return this.key[this.last];
	}

	public char removeFirstChar() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}

		final char[] key = this.key;
		final int pos = this.first;

		final char k = key[pos];

		this.removeEntry(pos);

		return k;
	}

	public char removeLastChar() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}

		final char[] key = this.key;
		final int pos = this.last;

		final char k = key[pos];

		this.removeEntry(pos);

		return k;
	}


	@Override
	public CharSortedSet subSet(final char from, final char to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CharSortedSet headSet(final char to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CharSortedSet tailSet(final char from) {
		throw new UnsupportedOperationException();
	}


	@Override
	public CharBidirectionalIterator iterator() {
		return new SetIterator();
	}

	@Override
	public CharBidirectionalIterator iterator(final char from) {
		return new SetIterator(from);
	}

	@Override
	public void forEach(final CharConsumer action) {
		final char[] key = this.key;

		final int nulll = this.nulll;
		final int last = this.last;

		int curr;
		int next = this.first;
		while (next != -1) {
			curr = next;
			while (++next < last && (key[next] == (char) 0 && next != nulll)) {
			}
			if (last < next) {
				next = -1;
			}

			action.accept(key[curr]);
		}
	}


	protected void resort(final int newN) {
		final char[] key = this.key;
		final char[] newKey = new char[newN];

		final int nulll = this.nulll;
		final int last = this.last;

		final int size = this.size;
		final RandomGenerator random = this.random;

		int required = size;

		int i = last + 1;
		int j = newN + 1;
		while (0 < required) {
			if (random.nextInt(--j) < required) {
				char o;
				while ((o = key[--i]) == (char) 0 && i != nulll) {
				}

				final int pos = j - 1;

				newKey[pos] = o;
				if (o == (char) 0) {
					this.nulll = pos;
				}

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

	protected long sparseBinarySearch(final char k) {
		final char[] key = this.key;

		final int nulll = this.nulll;

		int low = this.first;
		int high = this.last;

		while (true) {
			int mid = (low + high) >>> 1;

			char o;
			int sign = -1;
			int distance = 0;
			while ((o = key[mid]) == (char) 0 && mid != nulll) {
				sign = -sign;
				mid += (sign * ++distance);
			}

			if (mid == low || mid == high) {
				return ((high & 0xffffffffL) << Integer.SIZE) | (low & 0xffffffffL);
			}

			assert low < mid && mid < high;
			assert this.compare(key[low], k) < 0 && this.compare(k, key[high]) < 0;

			final int compare = this.compare(o, k);
			if (compare < 0) { // mid < k
				low = mid;
			} else if (compare > 0) { // k < mid
				high = mid;
			} else { // k == mid
				return ((mid & 0xffffffffL) << Integer.SIZE) | (mid & 0xffffffffL);
			}
		}
	}


	private final class SetIterator implements CharBidirectionalIterator {
		int prev = -1;
		int next = -1;
		int curr = -1;

		SetIterator() {
			this.next = CharSortedSparseArraySet.this.first;
		}

		SetIterator(final char from) {
			final char[] key = CharSortedSparseArraySet.this.key;
			switch (CharSortedSparseArraySet.this.size) {
				case 0 -> throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				case 1 -> {
					final int first = CharSortedSparseArraySet.this.first;
					final int last = CharSortedSparseArraySet.this.last;

					final int compare = CharSortedSparseArraySet.this.compare(from, key[first]);
					if (compare == 0) { // k == first == last
						this.prev = last;
						return;
					}

					throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				}
				case 2 -> {
					final int first = CharSortedSparseArraySet.this.first;
					final int last = CharSortedSparseArraySet.this.last;

					int compare;

					compare = CharSortedSparseArraySet.this.compare(from, key[first]);
					if (compare < 0) { // k < first
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == first
						this.prev = first;
						this.next = last;
						return;
					}

					compare = CharSortedSparseArraySet.this.compare(key[last], from);
					if (compare < 0) { // last < k
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == last
						this.prev = last;
						return;
					}

					throw new NoSuchElementException("The key " + from + " does not belong to this set.");
				}
				default -> {
					final int nulll = CharSortedSparseArraySet.this.nulll;
					final int first = CharSortedSparseArraySet.this.first;
					final int last = CharSortedSparseArraySet.this.last;

					int compare;

					compare = CharSortedSparseArraySet.this.compare(from, key[first]);
					if (compare < 0) { // k < first
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == first
						this.prev = this.next = first;
						while (key[++this.next] == (char) 0 && this.next != nulll) {
						}
						return;
					}

					compare = CharSortedSparseArraySet.this.compare(key[last], from);
					if (compare < 0) { // last < k
						throw new NoSuchElementException("The key " + from + " does not belong to this set.");
					} else if (compare == 0) { // k == last
						this.prev = last;
						return;
					}

					final long packed = CharSortedSparseArraySet.this.sparseBinarySearch(from);
					final int pos = (int) packed;
					if (pos == (int) (packed >>> Integer.SIZE)) {
						this.prev = this.next = pos;
						while (++this.next < last && (key[this.next] == (char) 0 && this.next != nulll)) {
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
		public char previousChar() {
			if (!this.hasPrevious()) {
				throw new NoSuchElementException();
			}

			final char[] key = CharSortedSparseArraySet.this.key;

			final int nulll = CharSortedSparseArraySet.this.nulll;
			final int first = CharSortedSparseArraySet.this.first;

			this.curr = this.prev;
			while (first < --this.prev && (key[this.prev] == (char) 0 && this.prev != nulll)) {
			}
			if (this.prev < first) {
				this.prev = -1;
			}
			this.next = this.curr;

			return key[this.curr];
		}

		@Override
		public char nextChar() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}

			final char[] key = CharSortedSparseArraySet.this.key;

			final int nulll = CharSortedSparseArraySet.this.nulll;
			final int last = CharSortedSparseArraySet.this.last;

			this.curr = this.next;
			while (++this.next < last && (key[this.next] == (char) 0 && this.next != nulll)) {
			}
			if (last < this.next) {
				this.next = -1;
			}
			this.prev = this.curr;

			return key[this.curr];
		}

		@Override
		public void remove() {
			if (this.curr == -1) {
				throw new IllegalStateException();
			}

			final char[] key = CharSortedSparseArraySet.this.key;

			final int pos = this.curr;

			final int nulll = CharSortedSparseArraySet.this.nulll;
			final int first = CharSortedSparseArraySet.this.first;
			final int last = CharSortedSparseArraySet.this.last;

			if (pos == nulll) {
				CharSortedSparseArraySet.this.nulll = -1;
			}

			if (pos == this.prev) {
				while (first < --this.prev && (key[this.prev] == (char) 0 && this.prev != nulll)) {
				}
				if (this.prev < first) {
					this.prev = -1;
				}
			} else if (pos == this.next) {
				while (++this.next < last && (key[this.next] == (char) 0 && this.next != nulll)) {
				}
				if (last < this.next) {
					this.next = -1;
				}
			}

			this.curr = -1;

			--CharSortedSparseArraySet.this.size;

			if (this.prev == -1) {
				CharSortedSparseArraySet.this.first = this.next;
			}
			if (this.next == -1) {
				CharSortedSparseArraySet.this.last = this.prev;
			}

			key[pos] = (char) 0;
		}

		@Override
		public void forEachRemaining(final CharConsumer action) {
			final char[] key = CharSortedSparseArraySet.this.key;

			final int nulll = CharSortedSparseArraySet.this.nulll;
			final int last = CharSortedSparseArraySet.this.last;

			while (this.next != -1) {
				this.curr = this.next;
				while (++this.next < last && (key[this.next] == (char) 0) && this.next != nulll) {
				}
				if (last < this.next) {
					this.next = -1;
				}
				this.prev = this.curr;

				action.accept(key[this.curr]);
			}
		}
	}

}
