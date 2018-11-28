mod stream_de;

use read;
use de;

pub struct Deserializer<R> {
    read: R
}

impl<R> Deserializer<R>
    where
        R: read::Read
{
    pub fn new(read: R) -> Self {
        Deserializer {
            read: read
        }
    }

    pub fn into_iter<T>(self) -> stream_de::StreamDeserializer<R, T>
        where
            T: de::Deserialize
    {
        stream_de::StreamDeserializer::new(self)
    }

    fn parse_whitespace(&mut self) -> read::Result<Option<u8>> {
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
impl<'a> Deserializer<read::slice_read::SliceRead<'a>> {

    pub fn from_slice(bytes: &'a [u8]) -> Self {
        Deserializer::new(read::slice_read::SliceRead::new(bytes))
    }

}