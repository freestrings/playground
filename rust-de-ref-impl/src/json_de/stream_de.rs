use std::marker::PhantomData;

use de;
use json_de;
use read;

pub struct StreamDeserializer<R, T> {
    de: json_de::Deserializer<R>,
    output: PhantomData<T>,
}

impl<R, T> StreamDeserializer<R, T>
    where
        R: read::Read,
        T: de::Deserialize
{
    pub fn new(read: R) -> Self {
        StreamDeserializer {
            de: json_de::Deserializer::new(read),
            output: PhantomData,
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
            Ok(None) => None,
            Ok(Some(b)) => {
                let result = T::deserialize(&mut self.de);
                Some(match result {
                    Ok(value) => Ok(value),
                    Err(e) => Err(read::Error {}),
                })
            }
            Err(e) => Some(Err(e))
        }
    }
}
