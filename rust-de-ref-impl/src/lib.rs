mod de;
mod read;
mod json_de;

#[cfg(test)]
mod tests {
    use json_de;
    use read;
    //
    // use 하지 않으면 Read trait의 next, position등을 호출 할 수 없다.
    //
    use read::Read;

    #[test]
    fn slice_read() {
        let mut slice_read = read::slice_read::SliceRead::new("12345\n67890".as_bytes());

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
        let deserialize = json_de::Deserializer::from_slice("12345\n67890".as_bytes());
        deserialize.into_iter::<json_de::value::Value>();
    }
}
