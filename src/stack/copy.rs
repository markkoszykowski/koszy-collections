use crate::stack::common::{
    array_vec_struct, check_capacity, const_assert, impl_addition, impl_common, impl_dedup,
    impl_resize_with, impl_retain, impl_split_off, impl_subtraction, impl_traits,
};
use crate::stack::error::OutOfMemoryError;
use std::mem::MaybeUninit;
use std::ops::RangeBounds;

array_vec_struct! { CopyArrayVec, Copy }

impl<T, const N: usize> CopyArrayVec<T, N>
where
    T: Copy,
{
    impl_common! { CopyArrayVec, const }
    impl_addition! { CopyArrayVec, const }
    impl_subtraction! { CopyArrayVec, const }

    /// [`Vec::truncate`]
    #[inline]
    pub const fn truncate(&mut self, new_len: usize) {
        let len: usize = self.len;
        unsafe {
            self.len = if len < new_len { len } else { new_len };
        }
    }

    /// [`Vec::clear`]
    #[inline]
    pub const fn clear(&mut self) {
        unsafe {
            self.len = 0;
        }
    }

    impl_split_off! { CopyArrayVec, const }
    impl_resize_with! { CopyArrayVec }
    impl_retain! { CopyArrayVec, Copy }

    /// [`Vec::extend_from_within`]
    #[inline]
    pub fn extend_from_within<R>(&mut self, src: R) -> Result<(), OutOfMemoryError>
    where
        R: RangeBounds<usize>,
    {
        todo!()
    }
}

impl_dedup! { CopyArrayVec, Copy }

impl<T, const N: usize> CopyArrayVec<T, N>
where
    T: Copy,
{
    /// [`Vec::resize`]
    #[inline]
    #[track_caller]
    pub const fn resize<F>(&mut self, new_len: usize, value: T) -> Result<(), OutOfMemoryError> {
        let len: usize = self.len;

        check_capacity!(new_len);

        if len < new_len {
            unsafe {
                let mut ptr: *mut T = self.as_mut_ptr().add(len);
                let mut i: usize = 0;
                while i < (new_len - len) {
                    std::ptr::write(ptr.add(i), value);
                }
                self.len = new_len
            }
        } else {
            self.truncate(new_len);
        }
        Ok(())
    }

    /// [`Vec::extend_from_slice`]
    #[inline]
    #[track_caller]
    pub const fn extend_from_slice(&mut self, other: &[T]) -> Result<(), OutOfMemoryError> {
        check_capacity!(self.len + other.len());

        unsafe { self.append_elements(other) }
        Ok(())
    }
}

impl<T, const N: usize> ConvertArrayVec<N> for T
where
    T: Copy,
{
    #[inline]
    fn to_array_vec(s: &[T]) -> CopyArrayVec<T, N> {
        let mut v: CopyArrayVec<T, N> = CopyArrayVec::new();
        unsafe {
            std::ptr::copy_nonoverlapping(s.as_ptr(), v.as_mut_ptr(), s.len());
            v.set_len(s.len());
        }
        v
    }
}

impl<T, const N: usize> SpecCloneIntoArrayVec<T, N> for [T]
where
    T: Copy,
{
    fn clone_into(&self, target: &mut CopyArrayVec<T, N>) {
        target.clear();
        match target.extend_from_slice(self) {
            Ok(_) => {}
            Err(_) => panic!(),
        }
    }
}

impl<T, const N: usize> Copy for CopyArrayVec<T, N> where T: Copy {}

impl_traits! { CopyArrayVec, Copy }
