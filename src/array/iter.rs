use std::iter::FusedIterator;
use std::mem::MaybeUninit;
use std::ops::Range;

/// [`std::array::iter::IndexRange`]
pub(super) struct IndexRange {
    pub(super) start: usize,
    pub(super) end: usize,
}

impl IndexRange {
    /// [`std::array::iter::IndexRange::new_unchecked`]
    #[inline]
    pub(super) const unsafe fn new_unchecked(start: usize, end: usize) -> IndexRange {
        IndexRange { start, end }
    }

    /// [`std::array::iter::IndexRange::zero_to`]
    #[inline]
    pub(super) const fn zero_to(end: usize) -> IndexRange {
        IndexRange { start: 0, end }
    }

    /// [`std::array::iter::IndexRange::start`]
    #[inline]
    pub(super) const fn start(&self) -> usize {
        self.start
    }

    /// [`std::array::iter::IndexRange::end`]
    #[inline]
    pub(super) const fn end(&self) -> usize {
        self.end
    }

    /// [`std::array::iter::IndexRange::len`]
    #[inline]
    pub(super) const fn len(&self) -> usize {
        unsafe { self.end.unchecked_sub(self.start) }
    }

    /// [`std::array::iter::IndexRange::next_unchecked`]
    #[inline]
    unsafe fn next_unchecked(&mut self) -> usize {
        debug_assert!(self.start < self.end);

        let value = self.start;
        self.start = unsafe { value.unchecked_add(1) };
        value
    }

    /// [`std::array::iter::IndexRange::next_back_unchecked`]
    #[inline]
    unsafe fn next_back_unchecked(&mut self) -> usize {
        debug_assert!(self.start < self.end);

        let value: usize = unsafe { self.end.unchecked_sub(1) };
        self.end = value;
        value
    }

    /// [`std::array::iter::IndexRange::take_prefix`]
    #[inline]
    pub(super) fn take_prefix(&mut self, n: usize) -> IndexRange {
        let mid: usize = if n <= self.len() {
            unsafe { self.start.unchecked_add(n) }
        } else {
            self.end
        };
        let prefix: IndexRange = IndexRange {
            start: self.start,
            end: mid,
        };
        self.start = mid;
        prefix
    }

    /// [`std::array::iter::IndexRange::take_suffix`]
    #[inline]
    pub(super) fn take_suffix(&mut self, n: usize) -> IndexRange {
        let mid: usize = if n <= self.len() {
            unsafe { self.end.unchecked_sub(n) }
        } else {
            self.start
        };
        let suffix: IndexRange = IndexRange {
            start: mid,
            end: self.end,
        };
        self.end = mid;
        suffix
    }
}

impl Iterator for IndexRange {
    type Item = usize;

    /// [`std::array::iter::IndexRange::next`]
    #[inline]
    fn next(&mut self) -> Option<usize> {
        match self.len() {
            0 => None,
            _ => unsafe { Some(self.next_unchecked()) },
        }
    }

    /// [`std::array::iter::IndexRange::size_hint`]
    #[inline]
    fn size_hint(&self) -> (usize, Option<usize>) {
        let len: usize = self.len();
        (len, Some(len))
    }
}

impl DoubleEndedIterator for IndexRange {
    /// [`std::array::iter::IndexRange::next_back`]
    #[inline]
    fn next_back(&mut self) -> Option<usize> {
        match self.len() {
            0 => None,
            _ => unsafe { Some(self.next_back_unchecked()) },
        }
    }
}

impl ExactSizeIterator for IndexRange {
    /// [`std::array::iter::IndexRange::len`]
    #[inline]
    fn len(&self) -> usize {
        self.len()
    }
}

impl FusedIterator for IndexRange {}

/// [`std::array::iter::IntoIter`]
pub struct IntoIter<T, const N: usize> {
    pub(super) data: [MaybeUninit<T>; N],
    pub(super) alive: IndexRange,
}

impl<T, const N: usize> IntoIter<T, N> {
    /// [`std::array::iter::IntoIter::new_unchecked`]
    #[inline]
    pub const unsafe fn new_unchecked(
        buffer: [MaybeUninit<T>; N],
        initialized: Range<usize>,
    ) -> IntoIter<T, N> {
        let alive: IndexRange =
            unsafe { IndexRange::new_unchecked(initialized.start, initialized.end) };
        IntoIter {
            data: buffer,
            alive,
        }
    }

    #[inline]
    const fn as_ptr(&self) -> *const T {
        unsafe { self.data.as_ptr().add(self.alive.start()) as *const T }
    }

    #[inline]
    const fn as_mut_ptr(&mut self) -> *mut T {
        unsafe { self.data.as_mut_ptr().add(self.alive.start()) as *mut T }
    }

    /// [`std::array::iter::IntoIter::as_slice`]
    #[inline]
    pub fn as_slice(&self) -> &[T] {
        unsafe { std::slice::from_raw_parts(self.as_ptr(), self.len()) }
    }

    /// [`std::array::iter::IntoIter::as_mut_slice`]
    #[inline]
    pub fn as_mut_slice(&mut self) -> &mut [T] {
        unsafe { std::slice::from_raw_parts_mut(self.as_mut_ptr(), self.len()) }
    }
}

impl<T, const N: usize> Iterator for IntoIter<T, N> {
    type Item = T;

    /// [`std::array::iter::IntoIter::next`]
    #[inline]
    fn next(&mut self) -> Option<T> {
        self.alive
            .next()
            .map(|idx| unsafe { self.data.get_unchecked(idx).assume_init_read() })
    }

    /// [`std::array::iter::IntoIter::size_hint`]
    #[inline]
    fn size_hint(&self) -> (usize, Option<usize>) {
        let len = self.len();
        (len, Some(len))
    }

    /// [`std::array::iter::IntoIter::count`]
    #[inline]
    fn count(self) -> usize {
        self.len()
    }

    /// [`std::array::iter::IntoIter::last`]
    #[inline]
    fn last(mut self) -> Option<T> {
        self.next_back()
    }

    /// [`std::array::iter::IntoIter::fold`]
    #[inline]
    fn fold<B, F>(mut self, init: B, mut fold: F) -> B
    where
        F: FnMut(B, T) -> B,
    {
        let data: &mut [MaybeUninit<T>; N] = &mut self.data;
        (&mut self.alive).fold(init, |acc, idx| {
            fold(acc, unsafe { data.get_unchecked(idx).assume_init_read() })
        })
    }
}

impl<T, const N: usize> DoubleEndedIterator for IntoIter<T, N> {
    /// [`std::array::iter::IntoIter::next_back`]
    #[inline]
    fn next_back(&mut self) -> Option<T> {
        self.alive
            .next_back()
            .map(|idx| unsafe { self.data.get_unchecked(idx).assume_init_read() })
    }

    /// [`std::array::iter::IntoIter::rfold`]
    #[inline]
    fn rfold<B, F>(mut self, init: B, mut rfold: F) -> B
    where
        F: FnMut(B, T) -> B,
    {
        let data: &mut [MaybeUninit<T>; N] = &mut self.data;
        (&mut self.alive).rfold(init, |acc, idx| {
            rfold(acc, unsafe { data.get_unchecked(idx).assume_init_read() })
        })
    }
}

impl<T, const N: usize> ExactSizeIterator for IntoIter<T, N> {
    /// [`std::array::iter::IntoIter::len`]
    #[inline]
    fn len(&self) -> usize {
        self.alive.len()
    }
}

impl<T, const N: usize> FusedIterator for IntoIter<T, N> {}

impl<T, const N: usize> Clone for IntoIter<T, N>
where
    T: Clone,
{
    /// [`std::array::iter::IntoIter::clone`]
    #[inline]
    fn clone(&self) -> IntoIter<T, N> {
        let mut clone: IntoIter<T, N> = IntoIter {
            data: [const { MaybeUninit::uninit() }; N],
            alive: IndexRange::zero_to(0),
        };

        for (src, dst) in std::iter::zip(self.as_slice(), &mut clone.data) {
            dst.write(src.clone());
            clone.alive = IndexRange::zero_to(clone.alive.end() + 1);
        }

        clone
    }
}

impl<T, const N: usize> Drop for IntoIter<T, N> {
    /// [`std::array::iter::IntoIter::drop`]
    #[inline]
    fn drop(&mut self) {
        let slice: *mut [T] = std::ptr::slice_from_raw_parts_mut(self.as_mut_ptr(), self.len());
        unsafe {
            std::ptr::drop_in_place(slice);
        }
    }
}

impl<T, const N: usize> std::fmt::Debug for IntoIter<T, N>
where
    T: std::fmt::Debug,
{
    /// [`std::array::iter::IntoIter::fmt`]
    #[inline]
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple("IntoIter").field(&self.as_slice()).finish()
    }
}
