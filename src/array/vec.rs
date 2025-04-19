use crate::array::common::{
    array_vec_struct, check_capacity, impl_addition, impl_common, impl_dedup, impl_resize,
    impl_resize_with, impl_retain, impl_split_off, impl_subtraction, impl_traits, SetLenOnDrop,
};
use crate::array::error::OutOfMemoryError;
use std::alloc::Layout;
use std::borrow::Cow;
use std::mem::MaybeUninit;
use std::ops::RangeBounds;

array_vec_struct! { ArrayVec }

impl<T, const N: usize> ArrayVec<T, N> {
    impl_common! { ArrayVec }
    impl_addition! { ArrayVec }
    impl_subtraction! { ArrayVec }

    /// [`Vec::truncate`]
    #[inline]
    pub fn truncate(&mut self, new_len: usize) {
        let len: usize = self.len;

        if len < new_len {
            return;
        }

        unsafe {
            let ptr: *mut T = self.as_mut_ptr().add(new_len);
            let slice: *mut [T] = std::ptr::slice_from_raw_parts_mut(ptr, len - new_len);
            self.len = new_len;

            std::ptr::drop_in_place(slice);
        }
    }

    /// [`Vec::clear`]
    #[inline]
    pub fn clear(&mut self) {
        let slice: *mut [T] = std::ptr::slice_from_raw_parts_mut(self.as_mut_ptr(), self.len);

        unsafe {
            self.len = 0;

            std::ptr::drop_in_place(slice);
        }
    }

    impl_split_off! { ArrayVec }
    impl_retain! { ArrayVec }
    impl_resize_with! { ArrayVec }

    #[inline]
    unsafe fn into_ptr(&mut self, n: usize) -> *mut T {
        let len: usize = self.len;

        let layout: Layout = match Layout::array::<T>(n) {
            Ok(layout) => layout,
            Err(e) => panic!("{}", e),
        };

        unsafe {
            self.len = 0;

            let ptr: *mut T = std::alloc::alloc(layout) as *mut T;
            std::ptr::copy_nonoverlapping(self.as_ptr(), ptr, len);
            ptr
        }
    }

    /// [`Vec::into_boxed_slice`]
    #[inline]
    #[track_caller]
    pub fn into_vec(mut self) -> Vec<T> {
        let len: usize = self.len;
        unsafe { Vec::from_raw_parts(self.into_ptr(N), len, N) }
    }

    /// [`Vec::into_boxed_slice`]
    #[inline]
    #[track_caller]
    pub fn into_boxed_slice(mut self) -> Box<[T]> {
        let len: usize = self.len;
        unsafe { Box::from_raw(std::ptr::slice_from_raw_parts_mut(self.into_ptr(len), len)) }
    }
}

impl_dedup! { ArrayVec }

impl<T, const N: usize> ArrayVec<T, N>
where
    T: Clone,
{
    /// [`Vec::extend_with`]
    #[track_caller]
    fn extend_with(&mut self, n: usize, value: T) {
        unsafe {
            let ptr: *mut T = self.as_mut_ptr();
            let mut local_len: SetLenOnDrop<'_> = SetLenOnDrop::new(&mut self.len);
            for _ in 0..(n - 1) {
                std::ptr::write(ptr.add(local_len.current_len()), value.clone());
                local_len.increment_len(1);
            }
            if 0 < n {
                std::ptr::write(ptr.add(local_len.current_len()), value);
                local_len.increment_len(1);
            }
        }
    }

    /// [`Vec::extend_from_slice`]
    #[track_caller]
    pub fn extend_from_slice(&mut self, other: &[T]) -> Result<(), OutOfMemoryError> {
        check_capacity!(self.len + other.len());

        unsafe {
            let ptr: *mut T = self.as_mut_ptr();
            let mut local_len: SetLenOnDrop<'_> = SetLenOnDrop::new(&mut self.len);
            for element in other {
                std::ptr::write(ptr.add(local_len.current_len()), element.clone());
                local_len.increment_len(1);
            }
        }
        Ok(())
    }

    /// [`Vec::extend_from_within`]
    #[track_caller]
    pub fn extend_from_within<R>(&mut self, range: R) -> Result<(), OutOfMemoryError>
    where
        R: RangeBounds<usize>,
    {
        todo!()
    }

    impl_resize! { ArrayVec }
}

impl<T, const N: usize> ConvertArrayVec<N> for T
where
    T: Clone,
{
    fn to_array_vec(slice: &[T]) -> ArrayVec<T, N> {
        struct DropGuard<'a, T, const N: usize> {
            vec: &'a mut ArrayVec<T, N>,
            num_init: usize,
        }
        impl<T, const N: usize> Drop for DropGuard<'_, T, N> {
            fn drop(&mut self) {
                unsafe {
                    self.vec.set_len(self.num_init);
                }
            }
        }
        let mut vec: ArrayVec<T, N> = ArrayVec::new();
        let mut guard: DropGuard<'_, T, N> = DropGuard {
            vec: &mut vec,
            num_init: 0,
        };
        let slots: &mut [MaybeUninit<T>] = guard.vec.spare_capacity_mut();
        for element in slice {
            slots[guard.num_init].write(element.clone());
            guard.num_init += 1;
        }
        core::mem::forget(guard);
        unsafe {
            vec.set_len(slice.len());
        }
        vec
    }
}

impl<T, const N: usize> SpecCloneIntoArrayVec<T, N> for [T]
where
    T: Clone,
{
    fn clone_into(&self, target: &mut ArrayVec<T, N>) {
        target.truncate(self.len());

        let (init, tail): (&[T], &[T]) = self.split_at(target.len());

        target.clone_from_slice(init);
        match target.extend_from_slice(tail) {
            Ok(_) => {}
            Err(_) => unreachable!(),
        }
    }
}

impl<T, const N: usize> Drop for ArrayVec<T, N> {
    /// [`Vec::drop`]
    #[inline]
    #[track_caller]
    fn drop(&mut self) {
        let slice: *mut [T] = std::ptr::slice_from_raw_parts_mut(self.as_mut_ptr(), self.len);
        unsafe {
            std::ptr::drop_in_place(slice);
        }
    }
}

impl_traits! { ArrayVec }

impl<T, const N: usize> From<ArrayVec<T, N>> for Vec<T> {
    /// [`Vec::from`]
    #[inline]
    #[track_caller]
    fn from(value: ArrayVec<T, N>) -> Vec<T> {
        value.into_vec()
    }
}

impl<T, const N: usize> From<ArrayVec<T, N>> for Box<[T]> {
    /// [`Box::from`]
    #[inline]
    #[track_caller]
    fn from(value: ArrayVec<T, N>) -> Box<[T]> {
        value.into_boxed_slice()
    }
}

impl<'a, T, const N: usize> From<ArrayVec<T, N>> for Cow<'a, [T]>
where
    T: Clone,
{
    /// [`Cow::from`]
    #[inline]
    #[track_caller]
    fn from(value: ArrayVec<T, N>) -> Cow<'a, [T]> {
        Cow::Owned(value.into_vec())
    }
}

impl<'a, T, const N: usize> From<&'a ArrayVec<T, N>> for Cow<'a, [T]>
where
    T: Clone,
{
    /// [`Cow::from`]
    #[inline]
    #[track_caller]
    fn from(value: &'a ArrayVec<T, N>) -> Cow<'a, [T]> {
        Cow::Borrowed(value.as_slice())
    }
}

#[macro_export]
macro_rules! array_vec {
    () => {
        $crate::array::vec::ArrayVec::new()
    };
    ($elem:expr; $n:expr) => {
        $crate::array::vec::ArrayVec::from_elem($elem, $n)
    };
    ($($x:expr),+ $(,)?) => {
        $crate::array::vec::ArrayVec::from([$($x),+])
    };
}
