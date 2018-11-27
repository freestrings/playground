use std::io;
use std::marker::PhantomData;

pub type Result<T> = io::Result<T>;

mod private {
    pub trait Sealed {}
}

pub trait Read<'my>: private::Sealed {
    fn next(&mut self) -> Result<Option<u8>>;
    fn position(&self) -> Position;
    fn peek(&mut self) -> Result<Option<u8>>;
    fn discard(&mut self);
}

pub struct SliceRead<'a> {
    slice: &'a [u8],
    index: usize,
}

impl<'a> SliceRead<'a> {
    pub fn new(slice: &'a [u8]) -> Self {
        SliceRead {
            slice: slice,
            index: 0,
        }
    }
}

impl<'a> private::Sealed for SliceRead<'a> {}

impl<'a> Read<'a> for SliceRead<'a> {
    fn next(&mut self) -> Result<Option<u8>> {
        Ok(if self.index < self.slice.len() {
            let ch = self.slice[self.index];
            self.index += 1;
            Some(ch)
        } else {
            None
        })
    }

    fn position(&self) -> Position {
        let mut position = Position { line: 1, column: 0 };
        for ch in &self.slice[..self.index] {
            match *ch {
                b'\n' => {
                    position.line += 1;
                    position.column = 0;
                }
                _ => {
                    position.column += 1;
                }
            }
        }
        position
    }

    fn peek(&mut self) -> Result<Option<u8>> {
        Ok(if self.index < self.slice.len() {
            Some(self.slice[self.index])
        } else {
            None
        })
    }

    fn discard(&mut self) {
        self.index += 1;
    }
}

pub struct Position {
    pub line: usize,
    pub column: usize,
}

pub struct Deserializer<R> {
    read: R
}

impl<'my, R> Deserializer<R>
    where
        R: Read<'my>
{
    pub fn new(read: R) -> Self {
        Deserializer {
            read: read
        }
    }
}

impl<'my, R> Deserializer<R>
    where
        R: Read<'my>
{
    pub fn into_iter<T>(self) -> StreamDeserializer<'my, R, T>
        where
            T: Deserialize<'my>
    {
        StreamDeserializer {
            de: self,
            output: PhantomData,
            lifetime: PhantomData,
        }
    }

    fn parse_whitespace(&mut self) -> Result<Option<u8>> {
        loop {
            match try!(self.read.peek()) {
                Some(b' ') | Some(b'\n') | Some(b'\t') | Some(b'\r') => {
                    self.read.discard();
                }
                other => {
                    return Ok(other);
                }
            }
        }
    }
}

///
/// Read 서브 타입중 SliceRead에 대한 구현
///
impl<'a> Deserializer<SliceRead<'a>> {
    pub fn from_slice(bytes: &'a [u8]) -> Self {
        Deserializer::new(SliceRead::new(bytes))
    }
}

pub trait Deserialize<'my> {}

pub struct StreamDeserializer<'my, R, T> {
    de: Deserializer<R>,
    output: PhantomData<T>,
    lifetime: PhantomData<&'my ()>,
}

impl<'my, R, T> StreamDeserializer<'my, R, T>
    where
        R: Read<'my>,
        T: Deserialize<'my>
{
    pub fn new(read: R) -> Self {
        StreamDeserializer {
            de: Deserializer::new(read),
            output: PhantomData,
            lifetime: PhantomData,
        }
    }
}

impl<'my, R, T> Iterator for StreamDeserializer<'my, R, T>
    where
        R: Read<'my>,
        T: Deserialize<'my>
{
    type Item = Result<T>;

    fn next(&mut self) -> Option<Self::Item> {
        match self.de.parse_whitespace() {
            Ok(None) => {
                None
            }
            Ok(Some(b)) => {
                // Here!
            }
            Err(e) => Some(Err(e))
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn slice_read() {
        let mut slice_read = SliceRead::new("12345\n67890".as_bytes());

        let _ = slice_read.next();
        let position = slice_read.position();
        assert_eq!(position.line, 1);
        assert_eq!(position.column, 1);

        for _ in 0..5 {
            let _ = slice_read.next();
        }
        let position = slice_read.position();
        assert_eq!(position.line, 2);
        assert_eq!(position.column, 0);
    }

    #[test]
    fn deserialize() {
        let deserialize = Deserializer::from_slice("12345\n67890".as_bytes());
    }
}
