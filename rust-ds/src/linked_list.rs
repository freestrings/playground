type Link<T> = Option<Box<Node<T>>>;

struct Node<T> {
    data: T,
    next: Link<T>,
}

struct List<T> {
    head: Link<T>,
}

///
/// struct Iter<'a, T> { ... } 는 파라미터인 `T`에 생명주기를 명시하지 않았기 때문에
/// "error[E0309]: the parameter type `T` may not live long enough" 에러가 난다.
///
struct Iter<'a, T: 'a> {
    next: Option<&'a Node<T>>
}

impl<'a, T> List<T> {
    fn new() -> Self {
        List {
            head: None
        }
    }

    ///
    /// &mut self는 치환이 가능 할 뿐 값을 옮길 수는 없다. 그래서 mem::replace(..) 같은 방법으로
    /// 값을 치환 하고 원래 값을 옮겨담는 방법을 사용해야 한다.
    ///
    /// http://stackoverflow.com/questions/28258548/cannot-move-out-of-borrowed-content-when-trying-to-transfer-ownership
    /// - Option::take 구현은 내부적으로 mem::replace 이다.
    ///
    fn push(&mut self, v: T) {
        self.head = Some(Box::new(Node {
            data: v,
            next: self.head.take()
        }));
    }

    ///
    /// https://www.reddit.com/r/rust/comments/4cqq50/syntax_of_dereferencing_a_box/
    ///
    /// Box안의 값을 명시적으로 옮길 때는 `*`(dereferencing)을 한다.
    ///
    fn pop(&mut self) -> Option<T> {
        self.head.take().map(|_node/*Box*/| {
            let node = *_node;
            self.head = node.next;
            node.data
        })
    }

    fn peek(&self) -> Option<&T> {
        self.head.as_ref().map(|_node| {
            &_node.data
        })
    }

    ///
    /// self.head.map(..) 은 borrow 에러. peek와 마찬가지로 as_ref로 참조 해야한다.
    /// 그래서 node dereferencing은 두번 `**` 한다.
    /// 그리고 새 값은 head에 붙이기 때문에 iteration 순서는 거꾸로다.
    ///
    fn iter(&mut self) -> Iter<T> {
        Iter {
            next: self.head.as_ref().map(|node| &**node)
        }
    }
}

impl<'a, T> Iterator for Iter<'a, T> {
    type Item = &'a T;

    fn next(&mut self) -> Option<Self::Item> {
        self.next.map(|node| {
            self.next = node.next.as_ref().map(|node| &**node);
            &node.data
        })
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
    fn test_op() {
        let mut list = super::List::new();

        assert_eq!(list.pop(), None);
        assert_eq!(list.peek(), None);

        list.push(1);
        list.push(2);
        assert_eq!(list.peek(), Some(&2));

        assert_eq!(list.pop(), Some(2));
        assert_eq!(list.pop(), Some(1));

        list.push(3);
        assert_eq!(list.pop(), Some(3));

        assert_eq!(list.pop(), None);
    }

    #[test]
    fn test_iter() {
        let mut list = super::List::new();
        list.push(1);
        list.push(2);

        assert_eq!(list.peek(), Some(&2));

        let mut iter = list.iter();
        assert_eq!(iter.next(), Some(&2));
        assert_eq!(iter.next(), Some(&1));

        assert_eq!(list.peek(), Some(&2));
    }
}