use crate::array::common::check_capacity;
use crate::array::copy::CopyArrayVec;
use crate::array::error::{FromUtf16Error, FromUtf8Error, OutOfMemoryError};
use std::borrow::{Borrow, BorrowMut, Cow};
use std::ffi::{CStr, OsStr};
use std::fmt::{Debug, Display, Formatter, Write};
use std::hash::{Hash, Hasher};
use std::net::{SocketAddr, ToSocketAddrs};
use std::ops::{Add, Deref, DerefMut, Index, IndexMut, RangeBounds};
use std::path::Path;
use std::slice::SliceIndex;
use std::str::{FromStr, Utf8Chunks, Utf8Error};
use std::vec::IntoIter;

/// [`String`]
#[derive(PartialEq, PartialOrd, Eq, Ord)]
pub struct ArrayString<const N: usize> {
    vec: CopyArrayVec<u8, N>,
}

impl<const N: usize> ArrayString<N> {
    /// [`String::new`]
    #[inline]
    pub const fn new() -> ArrayString<N> {
        ArrayString {
            vec: CopyArrayVec::new(),
        }
    }

    /// [`String::from_raw_parts`]
    #[inline]
    pub unsafe fn from_raw_parts(
        buf: [std::mem::MaybeUninit<u8>; N],
        len: usize,
    ) -> ArrayString<N> {
        ArrayString {
            vec: unsafe { CopyArrayVec::from_raw_parts(buf, len) },
        }
    }

    /// [`String::from_utf8`]
    #[inline]
    pub const fn from_utf8(vec: CopyArrayVec<u8, N>) -> Result<ArrayString<N>, Utf8Error> {
        match std::str::from_utf8(vec.as_slice()) {
            Ok(_) => Ok(ArrayString { vec }),
            Err(e) => Err(e),
        }
    }

    /// [`String::from_utf8_unchecked`]
    #[inline]
    pub const unsafe fn from_utf8_unchecked(bytes: CopyArrayVec<u8, N>) -> ArrayString<N> {
        ArrayString { vec: bytes }
    }

    /// [`String::from_utf8_lossy`]
    #[inline]
    pub fn from_utf8_lossy(v: &[u8]) -> Result<ArrayString<N>, OutOfMemoryError> {
        let mut iter: Utf8Chunks = v.utf8_chunks();

        let first_valid: &str = if let Some(chunk) = iter.next() {
            let valid: &str = chunk.valid();
            if chunk.invalid().is_empty() {
                debug_assert_eq!(valid.len(), v.len());
                return Ok(ArrayString {
                    vec: CopyArrayVec::try_from(v)?,
                });
            }
            valid
        } else {
            return Ok(ArrayString::new());
        };

        let mut string: ArrayString<N> = ArrayString::new();
        string.push_str(first_valid)?;
        string.push(char::REPLACEMENT_CHARACTER)?;

        for chunk in iter {
            string.push_str(chunk.valid())?;
            if !chunk.invalid().is_empty() {
                string.push(char::REPLACEMENT_CHARACTER)?;
            }
        }

        Ok(string)
    }

    /// [`String::from_utf16`]
    #[inline]
    pub fn from_utf16(v: &[u16]) -> Result<ArrayString<N>, FromUtf16Error> {
        let mut string: ArrayString<N> = ArrayString::new();
        for c in char::decode_utf16(v.iter().cloned()) {
            string
                .push(c.map_err(|e| FromUtf16Error::DecodeUtf16(e))?)
                .map_err(|e| FromUtf16Error::OutOfMemory(e))?;
        }
        Ok(string)
    }

    /// [`String::from_utf16_lossy`]
    #[inline]
    pub fn from_utf16_lossy(v: &[u16]) -> Result<ArrayString<N>, OutOfMemoryError> {
        let mut string: ArrayString<N> = ArrayString::new();
        for c in char::decode_utf16(v.iter().cloned()) {
            string.push(c.unwrap_or(char::REPLACEMENT_CHARACTER))?;
        }
        Ok(string)
    }

    /// [`String::capacity`]
    #[inline]
    pub const fn capacity(&self) -> usize {
        self.vec.capacity()
    }

    /// [`String::len`]
    #[inline]
    pub const fn len(&self) -> usize {
        self.vec.len()
    }

    /// [`String::is_empty`]
    #[inline]
    pub const fn is_empty(&self) -> bool {
        self.vec.is_empty()
    }

    /// [`String::into_bytes`]
    #[inline]
    pub const fn into_bytes(self) -> CopyArrayVec<u8, N> {
        self.vec
    }

    /// [`String::as_bytes`]
    #[inline]
    pub const fn as_bytes(&self) -> &[u8] {
        self.vec.as_slice()
    }

    /// [`String::as_str`]
    #[inline]
    pub const fn as_str(&self) -> &str {
        unsafe { std::str::from_utf8_unchecked(self.vec.as_slice()) }
    }

    /// [`String::as_mut_str`]
    #[inline]
    pub const fn as_mut_str(&mut self) -> &mut str {
        unsafe { std::str::from_utf8_unchecked_mut(self.vec.as_mut_slice()) }
    }

    /// [`String::as_mut_vec`]
    #[inline]
    pub const unsafe fn as_mut_vec(&mut self) -> &mut CopyArrayVec<u8, N> {
        &mut self.vec
    }

    /// [`String::insert_bytes`]
    #[track_caller]
    const unsafe fn insert_bytes(&mut self, index: usize, bytes: &[u8]) {
        let len: usize = self.len();
        let bytes_len: usize = bytes.len();

        unsafe {
            std::ptr::copy(
                self.vec.as_ptr().add(index),
                self.vec.as_mut_ptr().add(index + bytes_len),
                len - index,
            );
            std::ptr::copy_nonoverlapping(
                bytes.as_ptr(),
                self.vec.as_mut_ptr().add(index),
                bytes_len,
            );
            self.vec.set_len(len + bytes_len);
        }
    }

    /// [`String::insert`]
    #[track_caller]
    pub fn insert(&mut self, index: usize, c: char) -> Result<(), OutOfMemoryError> {
        assert!(self.is_char_boundary(index));

        let mut bytes: [u8; 4] = [0; 4];
        let bytes: &[u8] = c.encode_utf8(&mut bytes).as_bytes();

        check_capacity!(self.len() + bytes.len());

        unsafe {
            self.insert_bytes(index, bytes);
        }
        Ok(())
    }

    /// [`String::insert_str`]
    #[track_caller]
    pub fn insert_str(&mut self, index: usize, str: &str) -> Result<(), OutOfMemoryError> {
        assert!(self.is_char_boundary(index));

        check_capacity!(self.len() + str.len());

        unsafe {
            self.insert_bytes(index, str.as_bytes());
        }
        Ok(())
    }

    /// [`String::push`]
    #[track_caller]
    pub const fn push(&mut self, c: char) -> Result<(), OutOfMemoryError> {
        match c.len_utf8() {
            1 => self.vec.push(c as u8),
            _ => self
                .vec
                .extend_from_slice(c.encode_utf8(&mut [0; 4]).as_bytes()),
        }
    }

    /// [`String::push_str`]
    #[track_caller]
    pub const fn push_str(&mut self, str: &str) -> Result<(), OutOfMemoryError> {
        self.vec.extend_from_slice(str.as_bytes())
    }

    /// [`String::pop`]
    #[track_caller]
    pub fn pop(&mut self) -> Option<char> {
        let c: char = self.chars().rev().next()?;
        let new_len: usize = self.len() - c.len_utf8();
        unsafe {
            self.vec.set_len(new_len);
        }
        Some(c)
    }

    /// [`String::remove`]
    #[track_caller]
    pub fn remove(&mut self, index: usize) -> char {
        let c: char = match self[index..].chars().next() {
            Some(ch) => ch,
            None => panic!("cannot remove a char from the end of a string"),
        };

        let next: usize = index + c.len_utf8();
        let len: usize = self.len();
        unsafe {
            std::ptr::copy(
                self.vec.as_ptr().add(next),
                self.vec.as_mut_ptr().add(index),
                len - next,
            );
            self.vec.set_len(len - (next - index));
        }
        c
    }

    /// [`String::truncate`]
    #[inline]
    #[track_caller]
    pub fn truncate(&mut self, new_len: usize) {
        if new_len <= self.len() {
            assert!(self.is_char_boundary(new_len));

            self.vec.truncate(new_len)
        }
    }

    /// [`String::clear`]
    #[inline]
    #[track_caller]
    pub const fn clear(&mut self) {
        self.vec.clear()
    }

    /// [`String::split_off`]
    #[track_caller]
    pub fn split_off<const M: usize>(&mut self, at: usize) -> ArrayString<M> {
        assert!(self.is_char_boundary(at));

        let other: CopyArrayVec<u8, M> = self.vec.split_off(at);
        unsafe { ArrayString::from_utf8_unchecked(other) }
    }

    /// [`String::retain`]
    #[track_caller]
    pub fn retain<F>(&mut self, mut f: F)
    where
        F: FnMut(char) -> bool,
    {
        struct SetLenOnDrop<'a, const N: usize> {
            string: &'a mut ArrayString<N>,
            index: usize,
            del_bytes: usize,
        }

        impl<const N: usize> Drop for SetLenOnDrop<'_, N> {
            fn drop(&mut self) {
                let new_len: usize = self.index - self.del_bytes;
                debug_assert!(new_len <= self.string.len());
                unsafe { self.string.vec.set_len(new_len) };
            }
        }

        let len: usize = self.len();
        let mut guard: SetLenOnDrop<'_, N> = SetLenOnDrop {
            string: self,
            index: 0,
            del_bytes: 0,
        };

        while guard.index < len {
            let c: char = unsafe {
                guard
                    .string
                    .get_unchecked(guard.index..len)
                    .chars()
                    .next()
                    .unwrap_unchecked()
            };
            let c_len: usize = c.len_utf8();

            if !f(c) {
                guard.del_bytes += c_len;
            } else if 0 < guard.del_bytes {
                c.encode_utf8(unsafe {
                    std::slice::from_raw_parts_mut(
                        guard.string.as_mut_ptr().add(guard.index - guard.del_bytes),
                        c_len,
                    )
                });
            }

            guard.index += c_len;
        }

        drop(guard);
    }

    /// [`String::replace_range`]
    #[track_caller]
    pub fn replace_range<R>(&mut self, _range: R, _str: &str)
    where
        R: RangeBounds<usize>,
    {
        todo!()
    }
}

impl<const N: usize> Default for ArrayString<N> {
    /// [`String::default`]
    #[inline]
    fn default() -> ArrayString<N> {
        ArrayString::new()
    }
}

impl<const N: usize> Clone for ArrayString<N> {
    /// [`String::clone`]
    #[inline]
    fn clone(&self) -> ArrayString<N> {
        ArrayString {
            vec: self.vec.clone(),
        }
    }

    /// [`String::clone_from`]
    #[inline]
    fn clone_from(&mut self, source: &ArrayString<N>) {
        self.vec.clone_from(&source.vec);
    }
}

impl<const N: usize> Copy for ArrayString<N> {}

impl<const N: usize> Debug for ArrayString<N> {
    /// [`String::fmt`]
    #[inline]
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Debug::fmt(&**self, f)
    }
}

impl<const N: usize> Display for ArrayString<N> {
    /// [`String::fmt`]
    #[inline]
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Display::fmt(&**self, f)
    }
}

impl<const N: usize> Hash for ArrayString<N> {
    /// [`String::hash`]
    #[inline]
    fn hash<H>(&self, hasher: &mut H)
    where
        H: Hasher,
    {
        Hash::hash(&**self, hasher)
    }
}

impl<const N: usize> AsRef<str> for ArrayString<N> {
    /// [`String::as_ref`]
    #[inline]
    fn as_ref(&self) -> &str {
        self
    }
}

impl<const N: usize> AsMut<str> for ArrayString<N> {
    /// [`String::as_mut`]
    #[inline]
    fn as_mut(&mut self) -> &mut str {
        self
    }
}

impl<const N: usize> AsRef<[u8]> for ArrayString<N> {
    /// [`String::as_ref`]
    #[inline]
    fn as_ref(&self) -> &[u8] {
        self.as_bytes()
    }
}

impl<const N: usize> AsRef<OsStr> for ArrayString<N> {
    /// [`String::as_ref`]
    #[inline]
    fn as_ref(&self) -> &OsStr {
        AsRef::as_ref(&**self)
    }
}

impl<const N: usize> AsRef<Path> for ArrayString<N> {
    /// [`String::as_ref`]
    #[inline]
    fn as_ref(&self) -> &Path {
        Path::new(self)
    }
}

impl<const N: usize> Deref for ArrayString<N> {
    type Target = str;

    /// [`String::deref`]
    #[inline]
    fn deref(&self) -> &str {
        self.as_str()
    }
}

impl<const N: usize> DerefMut for ArrayString<N> {
    /// [`String::deref_mut`]
    #[inline]
    fn deref_mut(&mut self) -> &mut str {
        self.as_mut_str()
    }
}

impl<const N: usize> Borrow<str> for ArrayString<N> {
    /// [`String::borrow`]
    #[inline]
    fn borrow(&self) -> &str {
        &self[..]
    }
}

impl<const N: usize> BorrowMut<str> for ArrayString<N> {
    /// [`String::borrow_mut`]
    #[inline]
    fn borrow_mut(&mut self) -> &mut str {
        &mut self[..]
    }
}

impl<I, const N: usize> Index<I> for ArrayString<N>
where
    I: SliceIndex<str>,
{
    type Output = I::Output;

    /// [`String::index`]
    #[inline]
    fn index(&self, index: I) -> &I::Output {
        Index::index(&**self, index)
    }
}

impl<I, const N: usize> IndexMut<I> for ArrayString<N>
where
    I: SliceIndex<str>,
{
    /// [`String::index_mut`]
    #[inline]
    fn index_mut(&mut self, index: I) -> &mut I::Output {
        IndexMut::index_mut(&mut **self, index)
    }
}

impl<const N: usize> Write for ArrayString<N> {
    /// [`String::write_str`]
    #[inline]
    fn write_str(&mut self, str: &str) -> std::fmt::Result {
        match self.push_str(str) {
            Ok(_) => Ok(()),
            Err(_) => Err(std::fmt::Error),
        }
    }

    /// [`String::write_char`]
    #[inline]
    fn write_char(&mut self, c: char) -> std::fmt::Result {
        match self.push(c) {
            Ok(_) => Ok(()),
            Err(_) => Err(std::fmt::Error),
        }
    }
}
macro_rules! impl_eq {
    ($lhs:ty, $rhs: ty) => {
        impl<const N: usize> PartialEq<$rhs> for $lhs {
            /// [`String::eq`]
            #[inline]
            fn eq(&self, other: &$rhs) -> bool {
                PartialEq::eq(&self[..], &other[..])
            }

            /// [`String::ne`]
            #[inline]
            fn ne(&self, other: &$rhs) -> bool {
                PartialEq::ne(&self[..], &other[..])
            }
        }

        impl<const N: usize> PartialEq<$lhs> for $rhs {
            /// [`String::eq`]
            #[inline]
            fn eq(&self, other: &$lhs) -> bool {
                PartialEq::eq(&self[..], &other[..])
            }

            /// [`String::ne`]
            #[inline]
            fn ne(&self, other: &$lhs) -> bool {
                PartialEq::ne(&self[..], &other[..])
            }
        }
    };
}

impl_eq! { ArrayString<N>, str }
impl_eq! { ArrayString<N>, &str }
impl_eq! { ArrayString<N>, String }
impl_eq! { ArrayString<N>, Cow<'_, str> }

impl<const N: usize> Add<&str> for ArrayString<N> {
    type Output = Result<ArrayString<N>, OutOfMemoryError>;

    /// [`String::add`]
    fn add(mut self, other: &str) -> Result<ArrayString<N>, OutOfMemoryError> {
        match self.push_str(other) {
            Ok(()) => Ok(self),
            Err(e) => Err(e),
        }
    }
}

macro_rules! impl_str_try_from {
    ($from:ty) => {
        impl<const N: usize> TryFrom<$from> for ArrayString<N> {
            type Error = OutOfMemoryError;

            /// [`String::from`]
            #[inline]
            #[track_caller]
            fn try_from(value: $from) -> Result<ArrayString<N>, OutOfMemoryError> {
                match CopyArrayVec::try_from(value.as_bytes()) {
                    Ok(vec) => Ok(unsafe { ArrayString::from_utf8_unchecked(vec) }),
                    Err(e) => Err(e),
                }
            }
        }
    };
}

macro_rules! impl_c_str_try_from {
    ($from:ty) => {
        impl<const N: usize> TryFrom<$from> for ArrayString<N> {
            type Error = FromUtf8Error;

            /// [`String::try_from`]
            #[inline]
            #[track_caller]
            fn try_from(value: $from) -> Result<ArrayString<N>, FromUtf8Error> {
                match value.to_str() {
                    Ok(str) => match CopyArrayVec::try_from(str.as_bytes()) {
                        Ok(vec) => Ok(unsafe { ArrayString::from_utf8_unchecked(vec) }),
                        Err(e) => Err(FromUtf8Error::OutOfMemory(e)),
                    },
                    Err(e) => Err(FromUtf8Error::Utf8(e)),
                }
            }
        }
    };
}

impl_str_try_from! { &str }
impl_str_try_from! { &mut str }

impl_c_str_try_from! { &CStr }
impl_c_str_try_from! { &mut CStr }

impl<const N: usize> FromStr for ArrayString<N> {
    type Err = OutOfMemoryError;

    /// [`String::from_str`]
    #[inline]
    #[track_caller]
    fn from_str(s: &str) -> Result<ArrayString<N>, OutOfMemoryError> {
        ArrayString::try_from(s)
    }
}

impl<const N: usize> From<ArrayString<N>> for CopyArrayVec<u8, N> {
    /// [`Vec::from`]
    #[inline]
    #[track_caller]
    fn from(value: ArrayString<N>) -> CopyArrayVec<u8, N> {
        value.into_bytes()
    }
}

impl<const N: usize> TryFrom<char> for ArrayString<N> {
    type Error = OutOfMemoryError;

    /// [`String::from`]
    #[inline]
    #[track_caller]
    fn try_from(c: char) -> Result<ArrayString<N>, OutOfMemoryError> {
        ArrayString::try_from(c.encode_utf8(&mut [0; 4]))
    }
}

impl<const N: usize> ToSocketAddrs for ArrayString<N> {
    type Iter = IntoIter<SocketAddr>;

    /// [`String::to_socket_addrs`]
    #[inline]
    #[track_caller]
    fn to_socket_addrs(&self) -> std::io::Result<IntoIter<SocketAddr>> {
        ToSocketAddrs::to_socket_addrs(&**self)
    }
}
