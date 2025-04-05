use std::error::Error;
use std::fmt::{Display, Formatter};

#[derive(Copy, Eq, PartialEq, Clone, Debug)]
pub struct OutOfMemoryError(pub(crate) ());

impl Display for OutOfMemoryError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "static length struct does not have enough memory")
    }
}

impl Error for OutOfMemoryError {}
