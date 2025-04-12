use crate::stack::common::{
    array_vec_struct, check_capacity, impl_addition, impl_common, impl_dedup, impl_resize,
    impl_resize_with, impl_retain, impl_split_off, impl_subtraction, impl_traits, SetLenOnDrop,
};
use crate::stack::error::OutOfMemoryError;
use std::mem::MaybeUninit;
use std::ops::RangeBounds;

array_vec_struct! { ArrayVec }

impl<T, const N: usize> ArrayVec<T, N> {
    impl_common! { ArrayVec }
    impl_addition! { ArrayVec }
    impl_subtraction! { ArrayVec }

    /// [`Vec::truncate`]
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
    pub fn extend_from_within<R>(&mut self, src: R) -> Result<(), OutOfMemoryError>
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
    fn to_array_vec(s: &[T]) -> ArrayVec<T, N> {
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
        for element in s {
            slots[guard.num_init].write(element.clone());
            guard.num_init += 1;
        }
        core::mem::forget(guard);
        unsafe {
            vec.set_len(s.len());
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
    fn drop(&mut self) {
        let slice: *mut [T] = std::ptr::slice_from_raw_parts_mut(self.as_mut_ptr(), self.len);
        unsafe {
            std::ptr::drop_in_place(slice);
        }
    }
}

impl_traits! { ArrayVec }

#[macro_export]
macro_rules! array_vec {
    () => {
        $crate::stack::vec::ArrayVec::new()
    };
    ($elem:expr; $n:expr) => {
        $crate::stack::vec::ArrayVec::from_elem($elem, $n)
    };
    ($($x:expr),+ $(,)?) => {
        $crate::stack::vec::ArrayVec::from([$($x),+])
    };
}
