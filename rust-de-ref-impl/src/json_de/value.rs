use std::fmt::{self, Debug, Formatter};

use de::{self, Visitor};

enum N {
    PosInt(u64),
    NegInt(i64),
    Float(f64),
}

pub struct Number {
    n: N,
}

impl Number {
    pub fn is_u64(&self) -> bool {
        match self.n {
            N::PosInt(_) => true,
            N::NegInt(_) | N::Float(_) => false,
        }
    }

    pub fn is_i64(&self) -> bool {
        match self.n {
            N::PosInt(v) => v <= i64::max_value() as u64,
            N::NegInt(_) => true,
            N::Float(_) => false,
        }
    }

    pub fn is_f64(&self) -> bool {
        match self.n {
            N::Float(_) => true,
            N::PosInt(_) | N::NegInt(_) => false,
        }
    }
}

pub enum Value {
    Bool(bool),
    Number(Number),
}

impl Debug for Value {
    fn fmt(&self, f: &mut Formatter) -> fmt::Result {
        unimplemented!()
    }
}

impl de::Deserialize for Value {
    fn deserialize<D>(deserializer: D) -> de::Result<Value>
        where D: de::Deserializer
    {
        struct ValueVisitor;

        impl Visitor for ValueVisitor {
            type Value = Value;

            fn visit_bool(self, v: bool) -> de::Result<Value> {
                unimplemented!()
            }

            fn visit_i32(self, v: i32) -> de::Result<Value> {
                unimplemented!()
            }
        }

        deserializer.deserialize_any(ValueVisitor)
    }
}

impl de::Deserializer for Value {
    fn deserialize_bool<V>(self, visitor: V) -> de::Result<V::Value>
        where V: Visitor
    {
        unimplemented!()
    }

    fn deserialize_i32<V>(self, visitor: V) -> de::Result<V::Value>
        where V: Visitor
    {
        unimplemented!()
    }

    fn deserialize_any<V>(self, visitor: V) -> de::Result<V::Value>
        where V: Visitor
    {
        unimplemented!()
    }
}
