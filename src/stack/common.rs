pub(super) const fn const_assert<const N: usize, const M: usize>() {
    assert!(N <= M, "N should be <= M");
}

macro_rules! array_vec_struct {
    ($vec:ident $(, $bound:ident)?) => {
        /// [`Vec`]
        pub struct $vec<T, const N: usize>
        where
            $(T: $bound,)?
        {
            data: [MaybeUninit<T>; N],
            len: usize,
        }
    };
}

macro_rules! impl_common {
    ($vec:ident $(, $is_const:ident)?) => {
        /// [`Vec::new`]
        #[inline]
        pub $($is_const)? fn new() -> $vec<T, N> {
            $vec {
                data: [const { MaybeUninit::uninit() }; N],
                len: 0,
            }
        }

        /// [`Vec::from_raw_parts`]
        #[inline]
        pub $($is_const)? unsafe fn from_raw_parts(data: [MaybeUninit<T>; N], len: usize) -> $vec<T, N> {
            $vec { data, len }
        }

        /// [`Vec::capacity`]
        #[inline]
        pub $($is_const)? fn capacity(&self) -> usize {
            N
        }

        /// [`Vec::len`]
        #[inline]
        pub $($is_const)? fn len(&self) -> usize {
            self.len
        }

        /// [`Vec::is_empty`]
        #[inline]
        pub $($is_const)? fn is_empty(&self) -> bool {
            self.len == 0
        }

        /// [`Vec::set_len`]
        #[inline]
        pub $($is_const)? unsafe fn set_len(&mut self, new_len: usize) {
            debug_assert!(new_len <= N);

            self.len = new_len;
        }

        /// [`Vec::as_ptr`]
        #[inline]
        pub $($is_const)? fn as_ptr(&self) -> *const T {
            self.data.as_ptr() as *const T
        }

        /// [`Vec::as_mut_ptr`]
        #[inline]
        pub $($is_const)? fn as_mut_ptr(&mut self) -> *mut T {
            self.data.as_mut_ptr() as *mut T
        }

        /// [`Vec::as_slice`]
        #[inline]
        pub $($is_const)? fn as_slice(&self) -> &[T] {
            unsafe { std::slice::from_raw_parts(self.as_ptr(), self.len) }
        }

        /// [`Vec::as_mut_slice`]
        #[inline]
        pub $($is_const)? fn as_mut_slice(&mut self) -> &mut [T] {
            unsafe { std::slice::from_raw_parts_mut(self.as_mut_ptr(), self.len) }
        }

        /// [`Vec::spare_capacity_mut`]
        #[inline]
        pub $($is_const)? fn spare_capacity_mut(&mut self) -> &mut [MaybeUninit<T>] {
            let len: usize = self.len;
            unsafe {
                std::slice::from_raw_parts_mut(
                    self.as_mut_ptr().add(len) as *mut MaybeUninit<T>,
                    N - len,
                )
            }
        }
    };
}

macro_rules! check_capacity {
    ($len:expr) => {
        if N < $len {
            return Err(OutOfMemoryError(()));
        }
    };
}

macro_rules! impl_addition {
    ($vec:ident $(, $is_const:ident)?) => {
        /// [`Vec::insert`]
        #[inline]
        #[track_caller]
        pub $($is_const)? fn insert(&mut self, index: usize, element: T) -> Result<(), OutOfMemoryError> {
            let len: usize = self.len;

            if len < index {
                panic!("insertion index should be <= len");
            }

            check_capacity!(len + 1);

            unsafe {
                let ptr: *mut T = self.as_mut_ptr().add(index);
                if index < len {
                    std::ptr::copy(ptr, ptr.add(1), len - index);
                }
                std::ptr::write(ptr, element);
                self.len += 1;
            }
            Ok(())
        }

        /// [`Vec::push`]
        #[inline]
        pub $($is_const)? fn push(&mut self, value: T) -> Result<(), OutOfMemoryError> {
            let len: usize = self.len;

            check_capacity!(len + 1);

            unsafe {
                let ptr: *mut T = self.as_mut_ptr().add(len);
                std::ptr::write(ptr, value);
                self.len += 1;
            }
            Ok(())
        }

        /// [`Vec::append`]
        #[inline]
        pub $($is_const)? fn append<const M: usize>(
            &mut self,
            other: &mut $vec<T, M>,
        ) -> Result<(), OutOfMemoryError> {
            check_capacity!(self.len + other.len);

            unsafe {
                self.append_elements(other.as_slice());
                other.len = 0;
            }
            Ok(())
        }

        /// [`Vec::append_elements`]
        #[inline]
        #[track_caller]
        $($is_const)? unsafe fn append_elements(&mut self, other: &[T]) {
            let len: usize = self.len;
            let other_len: usize = other.len();
            unsafe {
                std::ptr::copy_nonoverlapping(other.as_ptr(), self.as_mut_ptr().add(len), other_len)
            };
            self.len += other_len;
        }
    };
}

macro_rules! impl_subtraction {
    ($vec:ident $(, $is_const:ident)?) => {
        /// [`Vec::swap_remove`]
        #[inline]
        #[track_caller]
        pub $($is_const)? fn swap_remove(&mut self, index: usize) -> T {
            let len: usize = self.len;

            if len <= index {
                panic!("swap_remove index should be < len");
            }

            unsafe {
                let value: T = std::ptr::read(self.as_ptr().add(index));
                let ptr: *mut T = self.as_mut_ptr();
                std::ptr::copy(ptr.add(len - 1), ptr.add(index), 1);
                self.len -= 1;
                value
            }
        }

        /// [`Vec::remove`]
        #[inline]
        #[track_caller]
        pub $($is_const)? fn remove(&mut self, index: usize) -> T {
            let len: usize = self.len;

            if len <= index {
                panic!("removal index should be < len");
            }

            unsafe {
                let ptr: *mut T = self.as_mut_ptr().add(index);
                let value: T = std::ptr::read(ptr);
                std::ptr::copy(ptr.add(1), ptr, len - index - 1);
                self.len -= 1;
                value
            }
        }

        /// [`Vec::pop`]
        #[inline]
        pub $($is_const)? fn pop(&mut self) -> Option<T> {
            match self.len {
                0 => None,
                _ => unsafe {
                    self.len -= 1;
                    Some(std::ptr::read(self.as_ptr().add(self.len)))
                },
            }
        }
    };
}

macro_rules! impl_split_off {
    ($vec:ident $(, $is_const:ident)?) => {
        /// [`Vec::split_off`]
        #[inline]
        #[track_caller]
        pub $($is_const)? fn split_off<const M: usize>(&mut self, at: usize) -> $vec<T, M> {
            const_assert::<N, M>();

            let len: usize = self.len;

            if len < at {
                panic!("`at` split index should be <= len");
            }

            let mut other: $vec<T, M> = $vec::new();
            let other_len: usize = len - at;

            unsafe {
                self.len = at;
                other.len = other_len;

                std::ptr::copy_nonoverlapping(self.as_ptr().add(at), other.as_mut_ptr(), other_len);
            }
            other
        }
    };
}

macro_rules! impl_dedup {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, const N: usize> $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::dedup_by_key`]
            #[inline]
            pub fn dedup_by_key<F, K>(&mut self, mut key: F)
            where
                F: FnMut(&mut T) -> K,
                K: PartialEq,
            {
                self.dedup_by(|a, b| key(a) == key(b))
            }

            /// [`Vec::dedup_by`]
            pub fn dedup_by<F>(&mut self, mut same_bucket: F)
            where
                F: FnMut(&mut T, &mut T) -> bool,
            {
                let len: usize = self.len;
                if len <= 1 {
                    return;
                }

                let mut first_duplicate_idx: usize = 1;
                let start: *mut T = self.as_mut_ptr();
                while first_duplicate_idx != len {
                    let found_duplicate: bool = unsafe {
                        let prev: *mut T = start.add(first_duplicate_idx.wrapping_sub(1));
                        let current: *mut T = start.add(first_duplicate_idx);
                        same_bucket(&mut *current, &mut *prev)
                    };
                    if found_duplicate {
                        break;
                    }
                    first_duplicate_idx += 1;
                }
                if first_duplicate_idx == len {
                    return;
                }

                struct FillGapOnDrop<'a, T, const N: usize>
                where
                    $(T: $bound,)?
                {
                    read: usize,
                    write: usize,
                    vec: &'a mut $vec<T, N>,
                }

                impl<T, const N: usize> Drop for FillGapOnDrop<'_, T, N>
                where
                    $(T: $bound,)?
                {
                    fn drop(&mut self) {
                        unsafe {
                            let ptr: *mut T = self.vec.as_mut_ptr();
                            let len: usize = self.vec.len;

                            let items_left: usize = len.wrapping_sub(self.read);

                            let dropped_ptr: *mut T = ptr.add(self.write);
                            let valid_ptr: *mut T = ptr.add(self.read);

                            std::ptr::copy(valid_ptr, dropped_ptr, items_left);

                            let dropped: usize = self.read.wrapping_sub(self.write);

                            self.vec.len = len - dropped;
                        }
                    }
                }

                let mut gap: FillGapOnDrop<'_, T, N> = FillGapOnDrop {
                    read: first_duplicate_idx + 1,
                    write: first_duplicate_idx,
                    vec: self,
                };
                unsafe {
                    std::ptr::drop_in_place(start.add(first_duplicate_idx));
                }

                unsafe {
                    while gap.read < len {
                        let read_ptr: *mut T = start.add(gap.read);
                        let prev_ptr: *mut T = start.add(gap.write.wrapping_sub(1));

                        let found_duplicate: bool = same_bucket(&mut *read_ptr, &mut *prev_ptr);
                        if found_duplicate {
                            gap.read += 1;
                            std::ptr::drop_in_place(read_ptr);
                        } else {
                            let write_ptr: *mut T = start.add(gap.write);

                            std::ptr::copy_nonoverlapping(read_ptr, write_ptr, 1);

                            gap.write += 1;
                            gap.read += 1;
                        }
                    }

                    gap.vec.len = gap.write;
                    core::mem::forget(gap);
                }
            }
        }

        impl<T, const N: usize> $vec<T, N>
        where
            T: PartialEq $(+ $bound)?,
        {
            /// [`Vec::dedup`]
            #[inline]
            pub fn dedup(&mut self) {
                self.dedup_by(|a, b| a == b)
            }
        }
    };
}

macro_rules! impl_retain {
    ($vec:ident $(, $bound:ident)?) => {
        /// [`Vec::retain`]
        #[inline]
        pub fn retain<F>(&mut self, mut f: F)
        where
            F: FnMut(&T) -> bool,
        {
            self.retain_mut(|elem| f(elem));
        }

        /// [`Vec::retain_mut`]
        pub fn retain_mut<F>(&mut self, mut f: F)
        where
            F: FnMut(&mut T) -> bool,
        {
            let original_len: usize = self.len;

            if original_len == 0 {
                return;
            }

            self.len = 0;

            struct BackshiftOnDrop<'a, T, const N: usize>
            where
                $(T: $bound,)?
            {
                v: &'a mut $vec<T, N>,
                processed_len: usize,
                deleted_cnt: usize,
                original_len: usize,
            }

            impl<T, const N: usize> Drop for BackshiftOnDrop<'_, T, N>
            where
                $(T: $bound,)?
            {
                fn drop(&mut self) {
                    if 0 < self.deleted_cnt {
                        unsafe {
                            std::ptr::copy(
                                self.v.as_ptr().add(self.processed_len),
                                self.v
                                    .as_mut_ptr()
                                    .add(self.processed_len - self.deleted_cnt),
                                self.original_len - self.processed_len,
                            );
                        }
                    }

                    self.v.len = self.original_len - self.deleted_cnt;
                }
            }

            let mut g: BackshiftOnDrop<'_, T, N> = BackshiftOnDrop {
                v: self,
                processed_len: 0,
                deleted_cnt: 0,
                original_len,
            };

            fn process_loop<F, T, const N: usize, const DELETED: bool>(
                original_len: usize,
                f: &mut F,
                g: &mut BackshiftOnDrop<'_, T, N>,
            ) where
                F: FnMut(&mut T) -> bool,
                $(T: $bound,)?
            {
                while g.processed_len != original_len {
                    let cur: &mut T = unsafe { &mut *g.v.as_mut_ptr().add(g.processed_len) };
                    if !f(cur) {
                        g.processed_len += 1;
                        g.deleted_cnt += 1;

                        unsafe { std::ptr::drop_in_place(cur) };

                        if DELETED {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if DELETED {
                        unsafe {
                            let hole_slot: *mut T =
                                g.v.as_mut_ptr().add(g.processed_len - g.deleted_cnt);
                            std::ptr::copy_nonoverlapping(cur, hole_slot, 1);
                        }
                    }
                    g.processed_len += 1;
                }
            }

            process_loop::<F, T, N, false>(original_len, &mut f, &mut g);

            process_loop::<F, T, N, true>(original_len, &mut f, &mut g);

            core::mem::drop(g);
        }
    };
}

macro_rules! impl_resize_with {
    ($vec:ident) => {
        /// [`Vec::resize_with`]
        #[inline]
        #[track_caller]
        pub fn resize_with<F>(&mut self, new_len: usize, f: F) -> Result<(), OutOfMemoryError>
        where
            F: FnMut() -> T,
        {
            let len: usize = self.len;

            check_capacity!(new_len);

            if len < new_len {
                unsafe {
                    let ptr: *mut T = self.as_mut_ptr();
                    for element in core::iter::repeat_with(f).take(new_len - len) {
                        std::ptr::write(ptr.add(self.len), element);
                        self.len += 1
                    }
                }
            } else {
                self.truncate(new_len);
            }
            Ok(())
        }
    };
}

macro_rules! impl_clone {
    ($vec:ident $(, $bound:ident)?) => {
        trait ConvertArrayVec<const N: usize> {
            fn to_array_vec(s: &[Self]) -> $vec<Self, N>
            where
                Self: Sized $(+ $bound)?;
        }

        trait SpecCloneIntoArrayVec<T, const N: usize>
        where
            $(T: $bound,)?
        {
            fn clone_into(&self, target: &mut $vec<T, N>);
        }

        impl<T, const N: usize> Clone for $vec<T, N>
        where
            T: Clone $(+ $bound,)?
        {
            /// [`Vec::clone`]
            #[track_caller]
            fn clone(&self) -> $vec<T, N> {
                T::to_array_vec(&**self)
            }

            /// [`Vec::clone_from`]
            #[track_caller]
            fn clone_from(&mut self, source: &$vec<T, N>) {
                SpecCloneIntoArrayVec::clone_into(source.as_slice(), self);
            }
        }
    };
}

macro_rules! impl_default {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, const N: usize> Default for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::default`]
            #[inline]
            fn default() -> $vec<T, N> {
                $vec::new()
            }
        }
    };
}

macro_rules! impl_debug {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T: core::fmt::Debug, const N: usize> core::fmt::Debug for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::fmt`]
            fn fmt(&self, f: &mut core::fmt::Formatter<'_>) -> core::fmt::Result {
                core::fmt::Debug::fmt(&**self, f)
            }
        }
    };
}

macro_rules! impl_as_ref {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, const N: usize> AsRef<$vec<T, N>> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::as_ref`]
            fn as_ref(&self) -> &$vec<T, N> {
                self
            }
        }

        impl<T, const N: usize> AsMut<$vec<T, N>> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::as_mut`]
            fn as_mut(&mut self) -> &mut $vec<T, N> {
                self
            }
        }

        impl<T, const N: usize> AsRef<[T]> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::as_ref`]
            fn as_ref(&self) -> &[T] {
                self
            }
        }

        impl<T, const N: usize> AsMut<[T]> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::as_mut`]
            fn as_mut(&mut self) -> &mut [T] {
                self
            }
        }
    };
}

macro_rules! impl_deref {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, const N: usize> core::ops::Deref for $vec<T, N>
        where
            $(T: $bound,)?
        {
            type Target = [T];

            /// [`Vec::deref`]
            #[inline]
            fn deref(&self) -> &[T] {
                self.as_slice()
            }
        }

        impl<T, const N: usize> core::ops::DerefMut for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::deref_mut`]
            #[inline]
            fn deref_mut(&mut self) -> &mut [T] {
                self.as_mut_slice()
            }
        }
    };
}

macro_rules! impl_borrow {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, const N: usize> core::borrow::Borrow<[T]> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::borrow`]
            #[inline]
            fn borrow(&self) -> &[T] {
                &self[..]
            }
        }

        impl<T, const N: usize> core::borrow::BorrowMut<[T]> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::borrow_mut`]
            #[inline]
            fn borrow_mut(&mut self) -> &mut [T] {
                &mut self[..]
            }
        }
    };
}

macro_rules! impl_slice {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, I: std::slice::SliceIndex<[T]>, const N: usize> core::ops::Index<I> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            type Output = I::Output;

            /// [`Vec::index`]
            #[inline]
            fn index(&self, index: I) -> &I::Output {
                core::ops::Index::index(&**self, index)
            }
        }

        impl<T, I: std::slice::SliceIndex<[T]>, const N: usize> core::ops::IndexMut<I> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::index_mut`]
            fn index_mut(&mut self, index: I) -> &mut I::Output {
                core::ops::IndexMut::index_mut(&mut **self, index)
            }
        }
    };
}

macro_rules! impl_slice_eq {
    ([$($vars:tt)*], $lhs:ty, $rhs:ty $(, where $ty:ty: $bound:ident)*) => {
        impl<T, U, $($vars)*> PartialEq<$rhs> for $lhs
        where
            T: PartialEq<U>,
            $($ty: $bound,)*
        {
            /// [`Vec::eq`]
            #[inline]
            fn eq(&self, other: &$rhs) -> bool { self[..] == other[..] }
            /// [`Vec::ne`]
            #[inline]
            fn ne(&self, other: &$rhs) -> bool { self[..] != other[..] }
        }
    }
}
macro_rules! impl_hash {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T: core::hash::Hash, const N: usize> core::hash::Hash for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::hash`]
            #[inline]
            fn hash<H: core::hash::Hasher>(&self, state: &mut H) {
                core::hash::Hash::hash(&**self, state)
            }
        }
    };
}

macro_rules! impl_ord {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T: core::cmp::PartialOrd, const N: usize, const M: usize> core::cmp::PartialOrd<$vec<T, M>> for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::partial_cmp`]
            #[inline]
            fn partial_cmp(&self, other: &$vec<T, M>) -> Option<core::cmp::Ordering> {
                core::cmp::PartialOrd::partial_cmp(&**self, &**other)
            }
        }

        impl<T: core::cmp::Ord, const N: usize> core::cmp::Ord for $vec<T, N>
        where
            $(T: $bound,)?
        {
            /// [`Vec::cmp`]
            #[inline]
            fn cmp(&self, other: &$vec<T, N>) -> core::cmp::Ordering {
                core::cmp::Ord::cmp(&**self, &**other)
            }
        }
    };
}
macro_rules! impl_from {
    ($vec:ident $(, $bound:ident)?) => {
        impl<T, const N: usize, const M: usize> From<[T; N]> for $vec<T, M>
        where
            $(T: $bound,)?
        {
            /// [`Vec::from`]
            #[inline]
            fn from(value: [T; N]) -> $vec<T, M> {
                const_assert::<N, M>();

                let mut data: [MaybeUninit<T>; M] = [const { MaybeUninit::uninit() }; M];
                unsafe {
                    std::ptr::copy_nonoverlapping(value.as_ptr(), data.as_mut_ptr() as *mut T, N);
                }

                core::mem::forget(value);

                unsafe { $vec::from_raw_parts(data, N) }
            }
        }

        impl<T, const N: usize, const M: usize> From<&[T; N]> for $vec<T, M>
        where
            T: Clone $(+ $bound)?,
        {
            /// [`Vec::from`]
            #[inline]
            #[track_caller]
            fn from(value: &[T; N]) -> $vec<T, M> {
                const_assert::<N, M>();

                T::to_array_vec(value.as_slice())
            }
        }
        impl<T, const N: usize, const M: usize> From<&mut [T; N]> for $vec<T, M>
        where
            T: Clone $(+ $bound)?,
        {
            /// [`Vec::from`]
            #[inline]
            #[track_caller]
            fn from(value: &mut [T; N]) -> $vec<T, M> {
                const_assert::<N, M>();

                T::to_array_vec(value.as_mut_slice())
            }
        }

        impl<T, const N: usize> TryFrom<&[T]> for $vec<T, N>
        where
            T: Clone $(+ $bound)?,
        {
            type Error = OutOfMemoryError;

            /// [`Vec::from`]
            #[inline]
            #[track_caller]
            fn try_from(value: &[T]) -> Result<$vec<T, N>, OutOfMemoryError> {
                check_capacity!(value.len());

                Ok(T::to_array_vec(value))
            }
        }

        impl<T, const N: usize> TryFrom<&mut [T]> for $vec<T, N>
        where
            T: Clone $(+ $bound)?,
        {
            type Error = OutOfMemoryError;

            /// [`Vec::from`]
            #[inline]
            #[track_caller]
            fn try_from(value: &mut [T]) -> Result<$vec<T, N>, OutOfMemoryError> {
                check_capacity!(value.len());

                Ok(T::to_array_vec(value))
            }
        }
    };
}

macro_rules! impl_write {
    ($vec:ident) => {
        impl<const N: usize> std::io::Write for $vec<u8, N> {
            /// [`Vec<u8, A>::write`]
            #[inline]
            fn write(&mut self, mut buf: &[u8]) -> std::io::Result<usize> {
                buf = &buf[..std::cmp::min(buf.len(), self.capacity() - self.len())];
                match self.extend_from_slice(buf) {
                    Ok(_) => Ok(buf.len()),
                    Err(_) => unreachable!(),
                }
            }

            /// [`Vec<u8, A>::write_vectored`]
            #[inline]
            fn write_vectored(&mut self, bufs: &[std::io::IoSlice<'_>]) -> std::io::Result<usize> {
                let mut written: usize = 0;
                for buf in bufs {
                    match self.write(&buf) {
                        Ok(len) => {
                            written += len;
                            if len != buf.len() {
                                break;
                            }
                        }
                        Err(_) => unreachable!(),
                    }
                }
                Ok(written)
            }

            /// [`Vec<u8, A>::flush`]
            #[inline]
            fn flush(&mut self) -> std::io::Result<()> {
                Ok(())
            }

            /// [`Vec<u8, A>::write_all`]
            #[inline]
            fn write_all(&mut self, buf: &[u8]) -> std::io::Result<()> {
                match self.write(buf) {
                    Ok(len) if len == buf.len() => Ok(()),
                    Ok(_) => Err(std::io::Error::new(
                        std::io::ErrorKind::WriteZero,
                        "failed to write whole buffer",
                    )),
                    Err(_) => unreachable!(),
                }
            }
        }
    };
}
macro_rules! impl_traits {
    ($vec:ident $(, $bound:ident)?) => {
        $crate::stack::common::impl_clone! { $vec $(, $bound)? }

        $crate::stack::common::impl_default! { $vec $(, $bound)? }
        $crate::stack::common::impl_debug! { $vec $(, $bound)? }

        $crate::stack::common::impl_as_ref! { $vec $(, $bound)? }
        $crate::stack::common::impl_deref! { $vec $(, $bound)? }
        $crate::stack::common::impl_borrow! { $vec $(, $bound)? }
        $crate::stack::common::impl_slice! { $vec $(, $bound)? }

        $crate::stack::common::impl_slice_eq! { [const N: usize, const M: usize], $vec<T, N>, $vec<U, M> $(, where T: $bound)? $(, where U: $bound)? }

        $crate::stack::common::impl_slice_eq! { [const N: usize], $vec<T, N>, &[U] $(, where T: $bound)? }
        $crate::stack::common::impl_slice_eq! { [const N: usize], $vec<T, N>, &mut [U] $(, where T: $bound)? }
        $crate::stack::common::impl_slice_eq! { [const N: usize], &[T], $vec<U, N> $(, where U: $bound)? }
        $crate::stack::common::impl_slice_eq! { [const N: usize], &mut [T], $vec<U, N> $(, where U: $bound)? }

        $crate::stack::common::impl_slice_eq! { [const N: usize], $vec<T, N>, [U] $(, where T: $bound)? }
        $crate::stack::common::impl_slice_eq! { [const N: usize], [T], $vec<U, N> $(, where U: $bound)? }

        $crate::stack::common::impl_slice_eq! { [const N: usize], std::borrow::Cow<'_, [T]>, $vec<U, N>, where T: Clone $(, where T: $bound)? $(, where U: $bound)? }

        $crate::stack::common::impl_slice_eq! { [const N: usize, const M: usize], $vec<T, N>, [U; M] $(, where T: $bound)? }
        $crate::stack::common::impl_slice_eq! { [const N: usize, const M: usize], $vec<T, N>, &[U; M] $(, where T: $bound)? }

        impl<T: Eq, const N: usize> Eq for $vec<T, N> $(where T: $bound)? {}

        $crate::stack::common::impl_hash! { $vec $(, $bound)? }

        $crate::stack::common::impl_ord! { $vec $(, $bound)? }

        $crate::stack::common::impl_from! { $vec $(, $bound)? }

        $crate::stack::common::impl_write! { $vec }
    };
}

#[macro_export]
macro_rules! array_vec {
    () => (
        $crate::stack::vec::ArrayVec::new()
    );
    ($($x:expr),+ $(,)?) => {
        {
            // const COPY: bool = true $( && {
            //     const fn is_copy<T>(t: T) where T: Copy {}
            //     const _ = is_copy($x);
            //     true
            // } )*;
            const COPY: bool = true;

            if COPY {
                $crate::stack::copy::CopyArrayVec::from([$($x),+])
            } else {
                $crate::stack::vec::ArrayVec::from([$($x),+])
            }
        }
    };
}

pub(super) use check_capacity;

pub(super) use array_vec_struct;
pub(super) use impl_addition;
pub(super) use impl_as_ref;
pub(super) use impl_borrow;
pub(super) use impl_clone;
pub(super) use impl_common;
pub(super) use impl_debug;
pub(super) use impl_dedup;
pub(super) use impl_default;
pub(super) use impl_deref;
pub(super) use impl_from;
pub(super) use impl_hash;
pub(super) use impl_ord;
pub(super) use impl_resize_with;
pub(super) use impl_retain;
pub(super) use impl_slice;
pub(super) use impl_slice_eq;
pub(super) use impl_split_off;
pub(super) use impl_subtraction;
pub(super) use impl_traits;
pub(super) use impl_write;
