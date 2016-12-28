use std::vec::Vec;

fn postfix(mut v: Vec<char>) -> Vec<char> {
    v.reverse();

    let mut ret = Vec::new();

    let mut op_stack = Vec::new();
    while let Some(ch) = v.pop() {
        if ch == '*' || ch == '/' || ch == '+' || ch == '-' {
            op_stack.push(ch);
        } else if ch == '(' {
            // skip
        } else if ch == ')' {
            op_stack.pop().map(|op| ret.push(op));
        } else {
            ret.push(ch);
        }
    }

    while let Some(op) = op_stack.pop() {
        ret.push(op);
    }

    ret
}

#[cfg(test)]
mod tests {
    fn to_char(exp: String) -> Vec<char> {
        let mut v = Vec::new();
        for s in exp.split(' ') {
            v.push(s.chars().next().unwrap());
        }
        v
    }

    #[test]
    fn test1() {
        let ret = super::postfix(to_char("A + ( B * C )".to_string()));
        assert_eq!(ret, vec!['A', 'B', 'C', '*', '+']);
    }

    #[test]
    fn test2() {
        let ret = super::postfix(to_char("A + ( ( B * C ) / ( E - F ) )".to_string()));
        assert_eq!(ret, vec!['A', 'B', 'C', '*', 'E', 'F', '-', '/', '+']);
    }
}