use std::ptr;

type Link<T> = Option<Box<Node<T>>>;

///
/// tail은 Box 포인터가 아니라 Node의 mutable 참조를 보관한다.
/// Node가 mutable인 이유는 next값을 변경해야 하기 때문.
///
struct List<T> {
    head: Link<T>,
    tail: *mut Node<T>,
}

struct Node<T> {
    data: T,
    next: Link<T>,
}

struct Iter<'a, T: 'a> {
    next: Option<&'a Node<T>>,
}

struct IterMut<'a, T: 'a> {
    next: Option<&'a mut Node<T>>,
}

impl<T> List<T> {
    fn new() -> Self {
        List {
            head: None,
            tail: ptr::null_mut(),
        }
    }

    ///
    /// Box는 포인터이기 때문에 dereferencing 된다. 그리고 이 Box에서 벗겨낸 Node의 포인터를 얻는 방법이
    /// `let raw_tail: *mut _ = &mut *new_tail` 이다.
    /// raw_tail에 `*mut _` 포인터 타입을 지정하지 않으면 borrow가 되기 때문에 tail.next=Some(new_tail)에서 에러가 난다.
    ///
    /// 그리고 self.tail은 `*mut Node` 타입이기 때문에 (*self.tail).next로 참조를 해야 하며,
    /// raw 포인터이기 때문에 `unsafe`로 감싸야만 한다.
    ///
    fn push(&mut self, data: T) {
        let mut new_tail = Box::new(Node { data: data, next: None });

        let raw_tail: *mut _ = &mut *new_tail;
        //        let raw_tail = &mut *new_tail;

        if !self.tail.is_null() {
            unsafe {
                (*self.tail).next = Some(new_tail);
            }
        } else {
            self.head = Some(new_tail);
        }

        self.tail = raw_tail;
    }

    fn pop(&mut self) -> Option<T> {
        self.head.take().map(|head| {
            let head = *head;
            self.head = head.next;

            if self.head.is_none() {
                self.tail = ptr::null_mut();
            }

            head.data
        })
    }

    fn iter(&self) -> Iter<T> {
        Iter {
            next: self.head.as_ref().map(|node| &**node)
        }
    }

    fn iter_mut(&mut self) -> IterMut<T> {
        IterMut {
            next: self.head.as_mut().map(|node| &mut **node)
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

impl<'a, T> Iterator for IterMut<'a, T> {
    type Item = &'a mut T;

    fn next(&mut self) -> Option<Self::Item> {
        self.next.take().map(|node| {
            self.next = node.next.as_mut().map(|node| &mut **node);
            &mut node.data
        })
    }
}

impl<T> Drop for List<T> {
    fn drop(&mut self) {
        let mut link = self.head.take();
        while let Some(mut boxed) = link {
            link = boxed.next.take();
        }
    }
}

#[cfg(test)]
mod tests {
    use super::List;

    #[test]
    fn test_op() {
        let mut list = List::new();

        // Check empty list behaves right
        assert_eq!(list.pop(), None);

        // Populate list
        list.push(1);
        list.push(2);
        list.push(3);

        // Check normal removal
        assert_eq!(list.pop(), Some(1));
        assert_eq!(list.pop(), Some(2));

        // Push some more just to make sure nothing's corrupted
        list.push(4);
        list.push(5);

        // Check normal removal
        assert_eq!(list.pop(), Some(3));
        assert_eq!(list.pop(), Some(4));

        // Check exhaustion
        assert_eq!(list.pop(), Some(5));
        assert_eq!(list.pop(), None);

        // Check the exhaustion case fixed the pointer right
        list.push(6);
        list.push(7);

        // Check normal removal
        assert_eq!(list.pop(), Some(6));
        assert_eq!(list.pop(), Some(7));
        assert_eq!(list.pop(), None);
    }

    ///
    /// let mut list는 push가 &mut self 이기 때문에 mut로 선언.
    ///
    #[test]
    fn test_iter() {
        let mut list: List<i32> = List::new();
        list.push(1);
        list.push(2);

        let mut iter = list.iter();
        assert_eq!(iter.next(), Some(&1));
        assert_eq!(iter.next(), Some(&2));
        assert_eq!(iter.next(), None);
    }

    ///
    /// list에서 iter를 뽑고 나면 list에 push가 안된다.
    ///
    #[test]
    fn test_iter2() {
        let mut list: List<i32> = List::new();
        let mut iter = list.iter();
        assert_eq!(iter.next(), None);

        //        list.push(1);
    }

    ///
    /// list에서 iter_mut를 뽑고 나면 list에 push가 안된다.
    ///
    #[test]
    fn test_iter3() {
        let mut list: List<i32> = List::new();
        let mut iter = list.iter_mut();
        assert_eq!(iter.next(), None);

//        list.push(1);
    }
}



