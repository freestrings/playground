mod gcd_v1;
mod gcd_v2;

#[cfg(test)]
mod tests {
    #[test]
    fn get_gcd1() {
        println!("> get_gcd1");
        let v = super::gcd_v1::get_gcd(&mut (250 as i32), &mut (30 as i32));
        assert!(v == 10);
    }

    #[test]
    fn get_gcd2() {
        println!("> get_gcd2");
        let v = super::gcd_v2::get_gcd(&mut (250 as i32), &mut (30 as i32));
        assert!(v == 10);
    }

    #[test]
    fn swap_test() {
        println!("> swap");

        use std::mem;
        let mut x = 1;
        let mut y = 2;
        mem::swap(&mut x, &mut y);
        assert!(x == 2);
    }

}