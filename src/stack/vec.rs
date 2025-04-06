use crate::stack::common::{
    array_vec_struct, check_capacity, const_assert, impl_addition, impl_common, impl_dedup,
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
        let slice: *mut [T] = self.as_mut_slice() as *mut [T];

        unsafe {
            self.len = 0;

            std::ptr::drop_in_place(slice);
        }
    }

    impl_split_off! { ArrayVec }
    impl_resize_with! { ArrayVec }
    impl_retain! { ArrayVec }

    /// [`Vec::extend_from_within`]
    #[inline]
    pub fn extend_from_within<R>(&mut self, src: R) -> Result<(), OutOfMemoryError>
    where
        R: RangeBounds<usize>,
    {
        todo!()
    }
}

impl_dedup! { ArrayVec }

impl<T, const N: usize> ArrayVec<T, N>
where
    T: Clone,
{
    /// [`Vec::resize`]
    #[inline]
    #[track_caller]
    pub fn resize<F>(&mut self, new_len: usize, value: T) -> Result<(), OutOfMemoryError> {
        let len: usize = self.len;

        check_capacity!(new_len);

        if len < new_len {
            unsafe {
                let ptr: *mut T = self.as_mut_ptr();
                let mut local_len: SetLenOnDrop = SetLenOnDrop::new(&mut self.len);
                for _ in 0..(new_len - len - 1) {
                    std::ptr::write(ptr.add(local_len.current_len()), value.clone());
                    local_len.increment_len(1);
                }
                std::ptr::write(ptr.add(local_len.current_len()), value);
                local_len.increment_len(1);
            }
        } else {
            self.truncate(new_len);
        }
        Ok(())
    }

    /// [`Vec::extend_from_slice`]
    #[inline]
    #[track_caller]
    pub fn extend_from_slice(&mut self, other: &[T]) -> Result<(), OutOfMemoryError> {
        check_capacity!(self.len + other.len());

        unsafe {
            let ptr: *mut T = self.as_mut_ptr();
            let mut local_len: SetLenOnDrop = SetLenOnDrop::new(&mut self.len);
            for element in other {
                std::ptr::write(ptr.add(local_len.current_len()), element.clone());
                local_len.increment_len(1);
            }
        }
        Ok(())
    }
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
        impl<'a, T, const N: usize> Drop for DropGuard<'a, T, N> {
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
    #[inline]
    fn drop(&mut self) {
        unsafe {
            std::ptr::drop_in_place(std::ptr::slice_from_raw_parts_mut(
                self.as_mut_ptr(),
                self.len,
            ))
        }
    }
}

impl_traits! { ArrayVec }

#[cfg(test)]
mod tests {
    use crate::stack::vec::ArrayVec;
    use std::mem::MaybeUninit;

    #[test]
    fn new_test() {
        let mut vec: ArrayVec<i32, 32> = ArrayVec::new();
    }

    #[test]
    fn from_raw_parts_test() {
        let mut maybe_uninit: [MaybeUninit<u32>; 16] = [const { MaybeUninit::uninit() }; 16];
        let vec: ArrayVec<u32, 16> = unsafe {
            maybe_uninit.as_mut_ptr().write(MaybeUninit::new(1_000_000));
            ArrayVec::from_raw_parts(maybe_uninit, 1)
        };

        assert_eq!(vec, &[1_000_000]);
    }

    #[test]
    fn capacity_test() {
        let mut vec: ArrayVec<i32, 32> = ArrayVec::new();
        vec.push(42).unwrap();
        assert_eq!(vec.capacity(), 32);
    }

    #[test]
    fn len_test() {
        // let a: CopyArrayVec<i32, 32> = array_vec![1, 2, 3];
        // let a: ArrayVec<String, 32> = array_vec!["1".to_string(), "2".to_string(), "3".to_string()];
        // assert_eq!(a.len(), 3);
    }
}
