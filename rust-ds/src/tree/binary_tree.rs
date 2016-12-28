type Link<T> = Option<Box<Node<T>>>;

#[derive(Debug)]
struct Node<T> {
    data: T,
    left: Link<T>,
    right: Link<T>,
}

#[derive(Debug)]
struct BinaryTree<T> {
    root: Link<T>
}

impl<T: Ord> BinaryTree<T> {
    fn new() -> Self {
        BinaryTree { root: None }
    }

    ///
    /// 값이 들어갈 노드의 참조를 리턴한다. `&`로 `Link`의 참조를 순회한다.
    /// `{temp_node}`는 ... mutable more than once at a time. 에러를 방지한다.
    ///
    fn find_position_mut(&mut self, data: &T) -> &mut Link<T> {
        let mut temp_node = &mut self.root;
        loop {
            match { temp_node } {
                &mut Some(ref mut node) if node.data < *data => {
                    temp_node = &mut node.left;
                },
                &mut Some(ref mut l) if node.data >= *data => {
                    temp_node = &mut node.right;
                },
                other @ &mut Some(_) |
                other @ &mut None => return other
            }
        }
    }

    fn find_position(&self, data: &T) -> &Link<T> {
        let mut temp_node = &self.root;
        loop {
            match temp_node {
                &Some(ref node) if node.data == *data => {
                    return temp_node;
                },
                &Some(ref node) if node.data < *data => {
                    temp_node = &node.left;
                },
                &Some(ref node) if node.data > *data => {
                    temp_node = &node.right;
                },
                other @ &Some(_) |
                other @ &None => return other
            }
        }
    }
    ///
    /// `&mut`로 참조된 변수이기 때문에 dereferencing 된 `*target_node_ref`에 값을 새 값으로 치환 할 수 있다.
    ///
    fn insert(&mut self, data: T) {
        let target_node_ref = self.find_position_mut(&data);
        match target_node_ref {
            &mut Some(_) => {},
            &mut None => {
                *target_node_ref = Some(Box::new(Node { data: data, left: None, right: None }));
            }
        }
    }

    fn remove(&mut self, data: T) -> Link<T> {
        let mut temp_node = &mut self.root;
        loop {
            match { temp_node } {
                &mut Some(ref mut node) if node.data < *data => {
                    temp_node = &mut node.left;
                },
                &mut Some(ref mut l) if node.data > *data => {
                    temp_node = &mut node.right;
                },
                &mut Some(ref mut l) if node.data == *data => {

                },
                other @ &mut Some(_) |
                other @ &mut None => return other
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::BinaryTree;

    #[test]
    fn test_op() {
        let mut bt = BinaryTree::new();
        bt.insert(4);
        bt.insert(2);
        bt.insert(1);

        println!("{:?}", bt);
        let found_node = bt.find_position(&2);
        println!("{:?}", found_node);
    }
}