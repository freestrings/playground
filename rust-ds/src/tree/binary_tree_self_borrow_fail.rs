type Link<T> = Option<Box<Node<T>>>;

struct Node<T> {
    data: T,
    left: Link<T>,
    right: Link<T>,
}

struct BinaryTree<T> {
    root: Link<T>
}

impl<T: Ord> BinaryTree<T> {
    fn new() -> Self {
        BinaryTree { root: None }
    }

    fn traverse_and_insert(&mut self, parent: &mut Link<T>, data: T) {
        match parent {
            &mut Some(ref mut node) => {
            },
            _ => {}
        }
    }

    fn insert(&mut self, data: T) {
        let mut root = &mut self.root;
        self.traverse_and_insert(root, data);
    }
}