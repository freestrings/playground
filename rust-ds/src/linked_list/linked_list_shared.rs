use std::rc::Rc;

type Link<T> = Option<Rc<Node<T>>>;

struct Node<T> {
    data: T,
    next: Link<T>,
}

struct List<T> {
    head: Link<T>,
}

struct Iter<'a, T: 'a> {
    next: Option<&'a Node<T>>
}

impl<T> List<T> {
    fn new() -> Self {
        List { head: None }
    }

    ///
    /// `self.head.clone`는 Rc::clone() 이 아니라 Option의 clone이다.
    ///
    fn append(&self, data: T) -> List<T> {
        List {
            head: Some(Rc::new(Node {
                data: data,
                next: self.head.clone()
            }))
        }
    }

    ///
    /// `Option::map`과 `Option::and_then`에서 리턴된 값의 레이아웃 차이는
    /// map: match self { Some(x) => `Some(f(x))`, ... }
    /// and_then: match self { Some(`x`) => `f(x)`, ... }
    /// 와 같이 `Option`이 자동으로 감싸지는지 여부다.
    ///
    fn tail(&self) -> List<T> {
        List {
            head: self.head.as_ref().and_then(|node| node.next.clone())
        }
    }

    fn head(&self) -> Option<&T> {
        self.head.as_ref().map(|node| &node.data)
    }

    fn iter(&self) -> Iter<T> {
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

impl<T> Drop for List<T> {

    ///
    /// &mut self 이기 때문에 self.head.take()은 `let mut head`로 받아야 한다.
    /// 그렇지 않으면 immutable 필드를 mutable로 받을 수 없기 때문에 node.next.take()에서 에러가 난다.
    ///
    fn drop(&mut self) {
        let mut head = self.head.take();
        while let Some(node) = head {
            if let Ok(mut node) = Rc::try_unwrap(node) {
                head = node.next.take();
            } else {
                break;
            }
        }
    }

}

#[cfg(test)]
mod test {
    use super::List;

    #[test]
    fn test_op() {
        let list1 = List::new();
        assert_eq!(list1.head(), None);

        let list2 = list1.append(1).append(2).append(3);
        assert_eq!(list2.head(), Some(&3));

        let list = list2.tail();
        assert_eq!(list.head(), Some(&2));

        let list = list.tail();
        assert_eq!(list.head(), Some(&1));

        let list = list.tail();
        assert_eq!(list.head(), None);

        let list = list.tail();
        assert_eq!(list.head(), None);

        assert_eq!(list2.head(), Some(&3));
    }

    #[test]
    fn test_iter() {
        let list = List::new();
        let list = list.append(1).append(2).append(3);
        let mut iter = list.iter();
        assert_eq!(iter.next(), Some(&3));
    }
}