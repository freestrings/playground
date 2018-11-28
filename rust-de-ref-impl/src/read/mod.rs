pub mod slice_read;

pub struct Error {}

pub type Result<T> = std::result::Result<T, Error>;

pub trait Read {
    fn next(&mut self) -> Result<Option<u8>>;
    fn peek(&mut self) -> Result<Option<u8>>;
    fn discard(&mut self);
    fn position(&self) -> Position;
}

pub struct Position {
    pub line: usize,
    pub column: usize,
}