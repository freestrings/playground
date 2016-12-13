use std::rc::Rc;
use std::cell::{RefCell, Ref};

type Link<T> = Option<Rc<RefCell<Node<T>>>>;

struct Node<T> {
    data: T,
    next: Link<T>,
    prev: Link<T>,
}

struct List<T> {
    head: Link<T>,
    tail: Link<T>,
}

impl<T> Node<T> {
    fn new(data: T) -> Rc<RefCell<Self>> {
        Rc::new(RefCell::new(Node {
            data: data,
            next: None,
            prev: None,
        }))
    }
}

impl<T> List<T> {
    fn new() -> Self {
        List {
            head: None,
            tail: None,
        }
    }

    fn push_front(&mut self, data: T) {
        let node = Node::new(data);
        match self.head.take() {
            Some(old_head) => {
                old_head.borrow_mut().prev = Some(node.clone());
                node.borrow_mut().next = Some(old_head);
                self.head = Some(node);
            },
            None => {
                self.head = Some(node.clone());
                self.tail = Some(node);
            }
        }
    }

    ///
    /// RefCell이 래핑하고 있는 값을 얻으려고 `old_head.into_inner()` 하면 borrow 에러가 난다.
    /// 이때는 `Rc` 부터 차례로 값을 옮겨 와야한다 - Rc::try_unwrap() 은 참조가 1개면 래핑하고 있는 값을 `Result`로 리턴한다.
    /// Result를 Option으로 바꿀 때는 `ok()`를 사용하고,
    /// Option에서 값을 옮겨 올 때는 `unwrap()`을 사용 한다.
    /// 이렇게 RefCell을 얻었을땐 `into_inner()`로 RefCell의 값을 옮겨 올 수 있다.
    ///
    fn pop_front(&mut self) -> Option<T> {
        self.head.take().map(|old_head| {
            match old_head.borrow_mut().next.take() {
                Some(head) => {
                    head.borrow_mut().prev.take();
                    self.head = Some(head);
                },
                None => {
                    self.tail.take();
                }
            }

            Rc::try_unwrap(old_head).ok().unwrap().into_inner().data
        })
    }

    ///
    /// RefCell은 래핑하고 있는 값을 옮길 수 없다. 그래서 RefCell을 borrow 한 타입인 Ref 형으로 다시 래핑 해서 리턴한다.
    ///
    /// Ref는 PartialEq 같은 비교를 구현 할 수 없다.
    /// 그래서 Option을 unwrap 하고 Ref를 dereferencing 후 비교 해야 한다. test 코드 참고
    ///
    fn peek_front(&self) -> Option<Ref<T>> {
        //        self.head.as_ref().map(|node| &node.borrow().data)
        self.head.as_ref().map(|node| Ref::map(node.borrow(), |node| &node.data))
    }

    fn push_back(&mut self, data: T) {
        let node = Node::new(data);
        match self.tail.take() {
            Some(old_tail) => {
                old_tail.borrow_mut().next = Some(node.clone());
                node.borrow_mut().prev = Some(old_tail);
                self.tail = Some(node);
            },
            None => {
                self.tail = Some(node.clone());
                self.head = Some(node);
            }
        }
    }

    fn pop_back(&mut self) -> Option<T> {
        self.tail.take().map(|old_tail| {
            match old_tail.borrow_mut().prev.take() {
                Some(node) => {
                    node.borrow_mut().next.take();
                    self.tail = Some(node);
                },
                None => {
                    self.head.take();
                }
            }
            Rc::try_unwrap(old_tail).ok().unwrap().into_inner().data
        })
    }

    fn peek_back(&self) -> Option<Ref<T>> {
        self.tail.as_ref().map(|node| {
            Ref::map(node.borrow(), |node| &node.data)
        })
    }
}

impl<T> Drop for List<T> {
    fn drop(&mut self) {
        while self.pop_front().is_some() {}
    }
}

#[cfg(test)]
mod tests {
    use super::List;

    #[test]
    fn test_op_front() {
        let mut list = List::new();

        assert_eq!(list.pop_front(), None);

        list.push_front(1);
        list.push_front(2);
        list.push_front(3);

        assert_eq!(&*list.peek_front().unwrap(), &3);

        assert_eq!(list.pop_front(), Some(3));
        assert_eq!(list.pop_front(), Some(2));

        list.push_front(4);
        list.push_front(5);

        assert_eq!(list.pop_front(), Some(5));
        assert_eq!(list.pop_front(), Some(4));

        assert_eq!(list.pop_front(), Some(1));
        assert_eq!(list.pop_front(), None);
    }

    #[test]
    fn test_op_back() {
        let mut list = List::new();

        list.push_back(1);
        list.push_back(2);
        list.push_back(3);

        assert_eq!(list.pop_back(), Some(3));
        assert_eq!(list.pop_back(), Some(2));

        list.push_back(4);
        list.push_back(5);

        assert_eq!(list.pop_back(), Some(5));
        assert_eq!(list.pop_back(), Some(4));

        assert_eq!(*list.peek_back().unwrap(), 1);

        assert_eq!(list.pop_back(), Some(1));
        assert_eq!(list.pop_back(), None);
    }
}