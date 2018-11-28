pub struct Error {}

pub type Result<T> = std::result::Result<T, Error>;

pub trait Deserialize: Sized {
    fn deserialize<D>(deserializer: D) -> Result<Self>
        where
            D: Deserializer;
}

pub trait Deserializer: Sized {
    //
    // associated type `Value`가 정의 되지않아 컴파일 에러가 난다.
    //
    // fn deserialize_bool(self, visitor: Visitor) -> Result<Visitor::Value>;
    fn deserialize_bool<V>(self, visitor: V) -> Result<V::Value> where V: Visitor;
    fn deserialize_i32<V>(self, visitor: V) -> Result<V::Value> where V: Visitor;
    fn deserialize_any<V>(self, visitor: V) -> Result<V::Value> where V: Visitor;
}

pub trait Visitor: Sized {
    type Value;
    fn visit_bool(self, v: bool) -> Result<Self::Value>;
    fn visit_i32(self, v: i32) -> Result<Self::Value>;
}