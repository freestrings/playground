// https://ruslanspivak.com/lsbasi-part6/

use std::result;

#[derive(Debug, PartialEq)]
enum TokenType {
    Numeric,
    Plus,
    Minus,
    Mul,
    Div,
    LParen,
    RParen,
    Eof,
}

#[derive(Debug, PartialEq)]
struct Token {
    token: TokenType,
    value: String,
}

#[derive(Debug, PartialEq)]
pub enum Error {
    Eof,
    Position(usize),
}

pub struct Reader<'a> {
    input: &'a str,
    pos: usize,
    len: usize,
}

impl<'a> Reader<'a> {
    pub fn new(input: &'a str) -> Self {
        let len = input.chars().by_ref().map(|c| c.len_utf8()).sum();
        Reader {
            input,
            pos: 0,
            len,
        }
    }

    pub fn peek_char(&mut self) -> result::Result<char, Error> {
        let ch = self.input.chars().next().ok_or(Error::Eof)?;
        Ok(ch)
    }

    pub fn next_char(&mut self) -> result::Result<char, Error> {
        let ch = self.peek_char()?;
        self.input = &self.input[ch.len_utf8()..];
        let ret = Ok(ch);
        self.pos += ch.len_utf8();
        ret
    }
}

struct Lexer<'a> {
    reader: Reader<'a>
}

impl<'a> Lexer<'a> {
    fn new(text: &'a str) -> Self {
        Lexer { reader: Reader::new(text) }
    }

    fn numeric(&mut self, ch: char) -> String {
        let mut buf = String::new();
        buf.push(ch);
        loop {
            if let Ok(ch) = self.reader.peek_char() {
                if ch.is_numeric() {
                    buf.push(ch);
                    self.reader.next_char();
                    continue;
                }
            }
            break;
        }
        buf
    }

    fn next_token(&mut self) -> Token {
        while let Ok(ch) = self.reader.next_char() {
            if ch.is_whitespace() {
                continue;
            }

            if ch.is_numeric() {
                return Token { token: TokenType::Numeric, value: self.numeric(ch) };
            }

            let token_type = match ch {
                '+' => TokenType::Plus,
                '-' => TokenType::Minus,
                '*' => TokenType::Mul,
                '/' => TokenType::Div,
                '(' => TokenType::LParen,
                ')' => TokenType::RParen,
                _ => panic!("Invalid character")
            };

            return Token { token: token_type, value: ch.to_string() };
        };

        Token { token: TokenType::Eof, value: String::new() }
    }
}

struct Interpret<'a> {
    lexer: Lexer<'a>,
    current_token: Token,
}

impl<'a> Interpret<'a> {
    fn new(text: &'a str) -> Self {
        let mut lexer = Lexer::new(text);
        let current_token = lexer.next_token();
        Interpret { lexer: lexer, current_token: current_token }
    }

    fn eat(&mut self, token_type: TokenType) {
        if self.current_token.token == token_type {
            self.current_token = self.lexer.next_token();
        } else {
            panic!("Invalid syntax");
        }
    }

    fn factor(&mut self) -> isize {
        if self.current_token.token == TokenType::Numeric {
            let ret = self.current_token.value.parse::<isize>().unwrap();
            self.eat(TokenType::Numeric);
            return ret;
        } else if self.current_token.token == TokenType::LParen {
            self.eat(TokenType::LParen);
            let ret = self.expr();
            self.eat(TokenType::RParen);
            return ret;
        }
        unreachable!()
    }

    fn term(&mut self) -> isize {
        let mut ret = self.factor();
        loop {
            if self.current_token.token == TokenType::Mul {
                self.eat(TokenType::Mul);
                ret = ret * self.factor();
            } else if self.current_token.token == TokenType::Div {
                self.eat(TokenType::Div);
                ret = ret / self.factor();
            } else {
                break;
            }
        }
        ret
    }

    fn expr(&mut self) -> isize {
        let mut ret = self.term();

        loop {
            if self.current_token.token == TokenType::Plus {
                self.eat(TokenType::Plus);
                ret = ret + self.term();
            } else if self.current_token.token == TokenType::Minus {
                self.eat(TokenType::Minus);
                ret = ret - self.term();
            } else {
                break;
            }
        }

        return ret;
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn calc1() {
        let mut interpret = Interpret::new("1 + 2");
        let ret = interpret.expr();
        assert_eq!(ret, 3);

        let mut interpret = Interpret::new("11 + 22");
        let ret = interpret.expr();
        assert_eq!(ret, 33);
    }

    #[test]
    fn calc2() {
        let mut interpret = Interpret::new("2 * (2 + 3)");
        let ret = interpret.expr();
        assert_eq!(ret, 10);

        let mut interpret = Interpret::new("7 + 3 * (10 / (12 / (3 + 1) - 1))");
        let ret = interpret.expr();
        assert_eq!(ret, 22);
    }
}