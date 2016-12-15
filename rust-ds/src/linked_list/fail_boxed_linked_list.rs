type Link<T> = Option<Box<Node<T>>>;

///
/// tail은 Box 포인터가 아니라 Node의 mutable 참조를 보관한다.
/// Node가 mutable인 이유는 next값을 변경해야 하기 때문.
///
struct List<'a, T: 'a> {
    head: Link<T>,
    tail: Option<&'a mut Node<T>>,
}

struct Node<T> {
    data: T,
    next: Link<T>,
}

impl<'a, T> List<'a, T> {

    fn new() -> Self {
        List {
            head: None,
            tail: None,
        }
    }

    ///
    /// self.tail.next에 새 값을 추가할 것이기 때문에 self.tail 자리에 Node를 채운다.
    /// 그런 뒤, tail.next에 새로 생성한 Option(tail)을 할당한다.
    /// tail.next는 as_mut()로 가져와야 하고 Box 포인터와 as_mut()로 참조된 것을 dereferencing 해야 한다.
    /// 마지막으로 self.tail에 Box가 아닌 Option<&mut Node>를 할당한다.
    fn push(&'a mut self, data: T) {
        let tail = Box::new(Node { data: data, next: None });

        let new_tail = match self.tail.take() {
            Some(old_tail) => {
                old_tail.next = Some(tail);
                old_tail.next.as_mut().map(|node| &mut **node)
            },
            None => {
                self.head = Some(tail);
                self.head.as_mut().map(|node| &mut **node)
            }
        };

        self.tail = new_tail;
    }


    ///
    /// head는 Box이기 때문에 명시적으로 dereferencing 해야 한다.
    /// 그렇게 하지 않고 self.head = head.next하면 Box로 래핑된 값이 self.head에 할당 되기 때문에
    /// `value moved` 에러가 난다. ㄴ
    ///
    fn pop(&'a mut self) -> Option<T> {
        self.head.take().map(|head| {
            let head = *head;
            self.head = head.next;
            if self.head.is_none() {
                self.tail = None;
            }
            head.data
        })
    }
}

#[cfg(test)]
mod tests {
    use super::List;

    #[test]
    fn test_op() {

        let mut list = List::new();
        assert_eq!(list.pop(), None);

        list.push(1);
        list.push(2);

    }
}