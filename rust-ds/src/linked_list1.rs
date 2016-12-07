use std::mem;

enum Link {
    Empty,
    Normal(Box<Node>),
}

struct Node {
    data: i32,
    next: Link,
}

struct List {
    head: Link,
}

impl List {
    fn new() -> Self {
        List {
            head: Link::Empty
        }
    }

    ///
    /// &mut self는 치환이 가능 할 뿐 값을 옮길 수는 없다. 그래서 mem::replace(..) 같은 방법으로
    /// 값을 치환 하고 원래 값을 옮겨담는 방법을 사용해야 한다.
    ///
    /// http://stackoverflow.com/questions/28258548/cannot-move-out-of-borrowed-content-when-trying-to-transfer-ownership
    /// - Option::take 구현은 내부적으로 mem::replace 이다.
    ///
    fn push(&mut self, v: i32) {
        let link = mem::replace(&mut self.head, Link::Empty);
        self.head = Link::Normal(Box::new(Node {
            data: v,
            next: link
        }));
    }

    ///
    /// https://www.reddit.com/r/rust/comments/4cqq50/syntax_of_dereferencing_a_box/
    ///
    /// Box안의 값을 명시적으로 옮길 때는 `*`(dereferencing)을 한다.
    ///
    fn pop(&mut self) -> Option<i32> {
        let link = mem::replace(&mut self.head, Link::Empty);
        match link {
            Link::Empty => None,
            Link::Normal(_node/*Box*/) => {
                let node = *_node;
                self.head = node.next;
                Some(node.data)
            }
        }
    }
}

#[cfg(test)]
mod tests {
    #[test]
    fn swap_test() {
        use std::mem;

        let mut v: Vec<i32> = vec![1, 2];
        let old_v = mem::replace(&mut v, vec![3, 4, 5]);
        assert_eq!(v, [3, 4, 5]);
        assert_eq!(old_v, [1, 2]);
    }

    #[test]
    fn test() {
        let mut list = super::List::new();

        assert_eq!(list.pop(), None);

        list.push(1);
        list.push(2);
        assert_eq!(list.pop(), Some(2));
        assert_eq!(list.pop(), Some(1));

        list.push(3);
        assert_eq!(list.pop(), Some(3));

        assert_eq!(list.pop(), None);
    }
}