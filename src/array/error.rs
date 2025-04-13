use std::char::DecodeUtf16Error;
use std::error::Error;
use std::fmt::{Display, Formatter};
use std::str::Utf8Error;

#[derive(Copy, Eq, PartialEq, Clone, Debug)]
pub struct OutOfMemoryError(pub(crate) ());

impl Display for OutOfMemoryError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "static length struct does not have enough memory")
    }
}

impl Error for OutOfMemoryError {}

pub enum FromUtf8Error {
    OutOfMemory(OutOfMemoryError),
    Utf8(Utf8Error),
}

pub enum FromUtf16Error {
    OutOfMemory(OutOfMemoryError),
    DecodeUtf16(DecodeUtf16Error),
}
