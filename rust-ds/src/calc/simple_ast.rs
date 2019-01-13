// https://ruslanspivak.com/lsbasi-part7/

use std::result;

#[derive(Debug, PartialEq, Clone)]
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

#[derive(Debug, PartialEq, Clone)]
struct Token {
    ttype: TokenType,
    value: String,
}

#[derive(Debug, PartialEq)]
pub enum Error {
    Eof,
    Position(usize),
}

pub struct Reader<'a> {
    input: &'a str
}

impl<'a> Reader<'a> {
    pub fn new(input: &'a str) -> Self {
        Reader { input }
    }

    pub fn peek_char(&mut self) -> result::Result<char, Error> {
        let ch = self.input.chars().next().ok_or(Error::Eof)?;
        Ok(ch)
    }

    pub fn next_char(&mut self) -> result::Result<char, Error> {
        let ch = self.peek_char()?;
        self.input = &self.input[ch.len_utf8()..];
        Ok(ch)
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
                return Token { ttype: TokenType::Numeric, value: self.numeric(ch) };
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

            return Token { ttype: token_type, value: ch.to_string() };
        };

        Token { ttype: TokenType::Eof, value: String::new() }
    }
}

#[derive(Debug)]
struct Node {
    left: Option<Box<Node>>,
    right: Option<Box<Node>>,
    token: Token,
}

struct Parser<'a> {
    lexer: Lexer<'a>,
    current_token: Token,
}

impl<'a> Parser<'a> {
    fn new(text: &'a str) -> Self {
        let mut lexer = Lexer::new(text);
        let current_token = lexer.next_token();
        Parser { lexer: lexer, current_token: current_token }
    }

    fn eat(&mut self, token_type: TokenType) {
        if self.current_token.ttype == token_type {
            self.current_token = self.lexer.next_token();
        } else {
            panic!("Invalid syntax");
        }
    }

    fn factor(&mut self) -> Node {
        let current_token = self.current_token.clone();
        if current_token.ttype == TokenType::Numeric {
            self.eat(TokenType::Numeric);
            return Node {
                left: None,
                right: None,
                token: current_token,
            };
        } else if current_token.ttype == TokenType::LParen {
            self.eat(TokenType::LParen);
            let ret = self.expr();
            self.eat(TokenType::RParen);
            return ret;
        }
        unreachable!()
    }

    fn term(&mut self) -> Node {
        let mut ret = self.factor();
        loop {
            let current_token = self.current_token.clone();
            if current_token.ttype == TokenType::Mul {
                self.eat(TokenType::Mul);
            } else if current_token.ttype == TokenType::Div {
                self.eat(TokenType::Div);
            } else {
                break;
            }

            ret = Node {
                left: Some(Box::new(ret)),
                right: Some(Box::new(self.factor())),
                token: current_token,
            };
        }
        ret
    }

    fn expr(&mut self) -> Node {
        let mut ret = self.term();
        loop {
            let current_token = self.current_token.clone();
            if current_token.ttype == TokenType::Plus {
                self.eat(TokenType::Plus);
            } else if current_token.ttype == TokenType::Minus {
                self.eat(TokenType::Minus);
            } else {
                break;
            }

            ret = Node {
                left: Some(Box::new(ret)),
                right: Some(Box::new(self.term())),
                token: current_token,
            };
        }
        return ret;
    }

    fn parse(&mut self) -> Node {
        self.expr()
    }
}


trait NodeVisitor {
    fn visit(&self, node: Node) -> isize {
        match node.token.ttype {
            TokenType::Numeric => node.token.value.parse::<isize>().unwrap(),
            TokenType::Minus => {
                self.visit(*node.left.unwrap()) - self.visit(*node.right.unwrap())
            }
            TokenType::Plus => {
                self.visit(*node.left.unwrap()) + self.visit(*node.right.unwrap())
            }
            TokenType::Div => {
                self.visit(*node.left.unwrap()) / self.visit(*node.right.unwrap())
            }
            TokenType::Mul => {
                self.visit(*node.left.unwrap()) * self.visit(*node.right.unwrap())
            }
            _ => unreachable!()
        }
    }
}

struct Interpreter<'a> {
    input: &'a str
}

impl<'a> Interpreter<'a> {
    fn new(input: &'a str) -> Self {
        Interpreter { input }
    }

    fn interpret(&mut self) -> isize {
        let mut parser = Parser::new(self.input);
        let node = parser.parse();
        self.visit(node)
    }
}

impl<'a> NodeVisitor for Interpreter<'a> {}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn calc1() {
        let mut interpreter = Interpreter::new("1 + 2");
        let ret = interpreter.interpret();
        assert_eq!(ret, 3);

        let mut interpreter = Interpreter::new("11 + 22");
        let ret = interpreter.interpret();
        assert_eq!(ret, 33);
    }

    #[test]
    fn calc2() {
        let mut interpret = Interpreter::new("2 * (2 + 3)");
        let ret = interpret.interpret();
        assert_eq!(ret, 10);

        let mut interpret = Interpreter::new("7 + 3 * (10 / (12 / (3 + 1) - 1))");
        let ret = interpret.interpret();
        assert_eq!(ret, 22);
    }
}