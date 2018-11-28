use read;

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

impl<'a> read::Read for SliceRead<'a> {

    fn next(&mut self) -> read::Result<Option<u8>> {
        Ok(if self.index < self.slice.len() {
            let ch = self.slice[self.index];
            self.index += 1;
            Some(ch)
        } else {
            None
        })
    }

    fn peek(&mut self) -> read::Result<Option<u8>> {
        Ok(if self.index < self.slice.len() {
            Some(self.slice[self.index])
        } else {
            None
        })
    }

    fn discard(&mut self) {
        self.index += 1;
    }

    fn position(&self) -> read::Position {
        let mut position = read::Position { line: 1, column: 0 };
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
}