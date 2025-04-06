use crate::stack::common::check_capacity;
use crate::stack::copy::CopyArrayVec;
use crate::stack::error::OutOfMemoryError;
use std::borrow::{Borrow, BorrowMut, Cow};
use std::hash::{Hash, Hasher};
use std::ops::{Add, Deref, DerefMut, Index, IndexMut};
use std::slice::SliceIndex;
use std::str::{Utf8Chunks, Utf8Error};

/// [`String`]
#[derive(PartialEq, PartialOrd, Eq, Ord)]
struct ArrayString<const N: usize> {
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

        const REPLACEMENT: &str = "\u{FFFD}";

        let mut res: ArrayString<N> = ArrayString::new();
        res.push_str(first_valid)?;
        res.push_str(REPLACEMENT)?;

        for chunk in iter {
            res.push_str(chunk.valid())?;
            if !chunk.invalid().is_empty() {
                res.push_str(REPLACEMENT)?;
            }
        }

        Ok(res)
    }

    // /// [`String::from_utf16`]
    // #[inline]
    // pub fn from_utf16(v: &[u16]) -> Result<ArrayString<N>, FromUtf16Error> {
    //     let mut ret: ArrayString<N> = ArrayString::new();
    //     for c in char::decode_utf16(v.iter().cloned()) {
    //         if let Ok(c) = c {
    //             ret.push(c);
    //         } else {
    //             return Err(FromUtf16Error(()));
    //         }
    //     }
    //     Ok(ret)
    // }
    //
    // /// [`String::from_utf16_lossy`]
    // #[inline]
    // pub fn from_utf16_lossy(v: &[u16]) -> Result<String, FromUtf16Error> {
    //     todo!()
    // }

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
        self.len() == 0
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
    #[inline]
    const unsafe fn insert_bytes(&mut self, idx: usize, bytes: &[u8]) {
        let len: usize = self.len();
        let amt: usize = bytes.len();

        unsafe {
            std::ptr::copy(
                self.vec.as_ptr().add(idx),
                self.vec.as_mut_ptr().add(idx + amt),
                len - idx,
            );
            std::ptr::copy_nonoverlapping(bytes.as_ptr(), self.vec.as_mut_ptr().add(idx), amt);
            self.vec.set_len(len + amt);
        }
    }

    /// [`String::insert`]
    #[inline]
    pub fn insert(&mut self, idx: usize, ch: char) -> Result<(), OutOfMemoryError> {
        assert!(self.is_char_boundary(idx));

        let mut bits: [u8; 4] = [0; 4];
        let bits: &[u8] = ch.encode_utf8(&mut bits).as_bytes();

        check_capacity!(self.len() + bits.len());

        unsafe {
            self.insert_bytes(idx, bits);
        }
        Ok(())
    }

    /// [`String::insert_str`]
    #[inline]
    pub fn insert_str(&mut self, idx: usize, string: &str) -> Result<(), OutOfMemoryError> {
        assert!(self.is_char_boundary(idx));

        check_capacity!(self.len() + string.len());

        unsafe {
            self.insert_bytes(idx, string.as_bytes());
        }
        Ok(())
    }

    /// [`String::push`]
    #[inline]
    pub const fn push(&mut self, ch: char) -> Result<(), OutOfMemoryError> {
        match ch.len_utf8() {
            1 => self.vec.push(ch as u8),
            _ => self
                .vec
                .extend_from_slice(ch.encode_utf8(&mut [0; 4]).as_bytes()),
        }
    }

    /// [`String::push_str`]
    #[inline]
    pub const fn push_str(&mut self, string: &str) -> Result<(), OutOfMemoryError> {
        self.vec.extend_from_slice(string.as_bytes())
    }

    /// [`String::pop`]
    #[inline]
    pub fn pop(&mut self) -> Option<char> {
        let ch: char = self.chars().rev().next()?;
        let new_len: usize = self.len() - ch.len_utf8();
        unsafe {
            self.vec.set_len(new_len);
        }
        Some(ch)
    }

    /// [`String::remove`]
    #[inline]
    pub fn remove(&mut self, idx: usize) -> char {
        let ch: char = match self[idx..].chars().next() {
            Some(ch) => ch,
            None => panic!("cannot remove a char from the end of a string"),
        };

        let next: usize = idx + ch.len_utf8();
        let len: usize = self.len();
        unsafe {
            std::ptr::copy(
                self.vec.as_ptr().add(next),
                self.vec.as_mut_ptr().add(idx),
                len - next,
            );
            self.vec.set_len(len - (next - idx));
        }
        ch
    }

    /// [`String::truncate`]
    #[inline]
    pub fn truncate(&mut self, new_len: usize) {
        if new_len <= self.len() {
            assert!(self.is_char_boundary(new_len));

            self.vec.truncate(new_len)
        }
    }

    /// [`String::clear`]
    #[inline]
    pub const fn clear(&mut self) {
        self.vec.clear()
    }

    /// [`String::split_off`]
    #[inline]
    pub fn split_off<const M: usize>(&mut self, at: usize) -> ArrayString<M> {
        assert!(self.is_char_boundary(at));

        let other: CopyArrayVec<u8, M> = self.vec.split_off(at);
        unsafe { ArrayString::from_utf8_unchecked(other) }
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

impl<const N: usize> core::fmt::Display for ArrayString<N> {
    /// [`String::fmt`]
    #[inline]
    fn fmt(&self, f: &mut core::fmt::Formatter<'_>) -> core::fmt::Result {
        core::fmt::Display::fmt(&**self, f)
    }
}

impl<const N: usize> core::fmt::Debug for ArrayString<N> {
    /// [`String::fmt`]
    #[inline]
    fn fmt(&self, f: &mut core::fmt::Formatter<'_>) -> core::fmt::Result {
        core::fmt::Debug::fmt(&**self, f)
    }
}

impl<const N: usize> Hash for ArrayString<N> {
    /// [`String::hash`]
    #[inline]
    fn hash<H: Hasher>(&self, hasher: &mut H) {
        (**self).hash(hasher)
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
        self.as_str().index(index)
    }
}

impl<I, const N: usize> IndexMut<I> for ArrayString<N>
where
    I: SliceIndex<str>,
{
    /// [`String::index_mut`]
    #[inline]
    fn index_mut(&mut self, index: I) -> &mut I::Output {
        self.as_mut_str().index_mut(index)
    }
}

impl<const N: usize> core::fmt::Write for ArrayString<N> {
    /// [`String::write_str`]
    #[inline]
    fn write_str(&mut self, s: &str) -> core::fmt::Result {
        match self.push_str(s) {
            Ok(_) => Ok(()),
            Err(_) => Err(std::fmt::Error),
        }
    }

    /// [`String::write_char`]
    #[inline]
    fn write_char(&mut self, c: char) -> core::fmt::Result {
        match self.push(c) {
            Ok(_) => Ok(()),
            Err(_) => Err(std::fmt::Error),
        }
    }
}
macro_rules! impl_eq {
    ($lhs:ty, $rhs: ty) => {
        impl<'a, 'b, const N: usize> PartialEq<$rhs> for $lhs {
            #[inline]
            fn eq(&self, other: &$rhs) -> bool {
                PartialEq::eq(&self[..], &other[..])
            }
            #[inline]
            fn ne(&self, other: &$rhs) -> bool {
                PartialEq::ne(&self[..], &other[..])
            }
        }

        impl<'a, 'b, const N: usize> PartialEq<$lhs> for $rhs {
            #[inline]
            fn eq(&self, other: &$lhs) -> bool {
                PartialEq::eq(&self[..], &other[..])
            }
            #[inline]
            fn ne(&self, other: &$lhs) -> bool {
                PartialEq::ne(&self[..], &other[..])
            }
        }
    };
}

impl_eq! { ArrayString<N>, str }
impl_eq! { ArrayString<N>, &'a str }
impl_eq! { ArrayString<N>, String }
impl_eq! { ArrayString<N>, Cow<'a, str> }

impl<const N: usize> Add<&str> for ArrayString<N> {
    type Output = Result<ArrayString<N>, OutOfMemoryError>;

    /// [`String::add`]
    #[inline]
    fn add(mut self, other: &str) -> Result<ArrayString<N>, OutOfMemoryError> {
        match self.push_str(other) {
            Ok(_) => Ok(self),
            Err(e) => Err(e),
        }
    }
}
