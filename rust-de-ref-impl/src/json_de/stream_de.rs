use de;
use read;
use std::marker::PhantomData;

pub struct StreamDeserializer<R, T> {
    de: super::Deserializer<R>,
    output: PhantomData<T>,
}

impl<R, T> StreamDeserializer<R, T> {

    pub fn new(de: super::Deserializer<R>) -> Self {
        StreamDeserializer {
            de,
            output: PhantomData
        }
    }
}

impl<R, T> Iterator for StreamDeserializer<R, T>
    where
        R: read::Read,
        T: de::Deserialize
{
    type Item = read::Result<T>;

    fn next(&mut self) -> Option<Self::Item> {
        match self.de.parse_whitespace() {
            Ok(None) => {
                None
            }
            Ok(Some(b)) => {
                // Here!
                None
            }
            Err(e) => Some(Err(e))
        }
    }
}
