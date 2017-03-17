extern crate serde;
// extern crate serde_json;
// #[macro_use]
// extern crate serde_derive;

use std::collections::HashMap;
use std::io::Write;
use std::vec::Vec;
use serde::{Serialize, Serializer};
use serde::ser::*;

// #[derive(Clone, Debug, PartialEq, Serialize, Deserialize)]
struct Test {
    name: String,
    age: u32
}


impl Serializer for Test {
    fn serialize_bool(self, v: bool) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_i8(self, v: i8) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_i16(self, v: i16) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_i32(self, v: i32) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_i64(self, v: i64) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_u8(self, v: u8) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_u16(self, v: u16) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_u32(self, v: u32) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_u64(self, v: u64) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_f32(self, v: f32) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_f64(self, v: f64) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_char(self, v: char) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_str(self, value: &str) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_bytes(self, value: &[u8]) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_none(self) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_some<T: ? Sized + Serialize>(self, value: &T) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_unit(self) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_unit_struct(self, name: &'static str) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_unit_variant(self, name: &'static str, variant_index: usize, variant: &'static str) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_newtype_struct<T: ? Sized + Serialize>(self, name: &'static str, value: &T) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_newtype_variant<T: ? Sized + Serialize>(self, name: &'static str, variant_index: usize, variant: &'static str, value: &T) -> Result<Self::Ok, Self::Error> {
        unimplemented!()
    }

    fn serialize_seq(self, len: Option<usize>) -> Result<Self::SerializeSeq, Self::Error> {
        unimplemented!()
    }

    fn serialize_seq_fixed_size(self, size: usize) -> Result<Self::SerializeSeq, Self::Error> {
        unimplemented!()
    }

    fn serialize_tuple(self, len: usize) -> Result<Self::SerializeTuple, Self::Error> {
        unimplemented!()
    }

    fn serialize_tuple_struct(self, name: &'static str, len: usize) -> Result<Self::SerializeTupleStruct, Self::Error> {
        unimplemented!()
    }

    fn serialize_tuple_variant(self, name: &'static str, variant_index: usize, variant: &'static str, len: usize) -> Result<Self::SerializeTupleVariant, Self::Error> {
        unimplemented!()
    }

    fn serialize_map(self, len: Option<usize>) -> Result<Self::SerializeMap, Self::Error> {
        unimplemented!()
    }

    fn serialize_struct(self, name: &'static str, len: usize) -> Result<Self::SerializeStruct, Self::Error> {
        unimplemented!()
    }

    fn serialize_struct_variant(self, name: &'static str, variant_index: usize, variant: &'static str, len: usize) -> Result<Self::SerializeStructVariant, Self::Error> {
        unimplemented!()
    }
}

impl Serialize for Test {
    fn serialize<S>(&self, serialize: S) -> Result<S::Ok, S::Error> where S: Serializer {
        let s = serialize.serialize_struct("test", 2)?;

        s.end()
    }
}

fn main() {
    let test = Test {
        name: "Han".to_string(),
        age: 43
    };

    test.serialize()

}