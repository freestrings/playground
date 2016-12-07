#[allow(dead_code)]
fn is_prime1(n: i32) -> bool {
    for i in 2..n {
        if n % i == 0 {
            return false;
        }
    }
    true
}

#[allow(dead_code)]
fn is_prime2(n: i32) -> bool {
    let s = (n as f64).sqrt() as i32;
    for i in 2..s + 1 {
        if n % i == 0 {
            return false;
        }
    }
    true
}

#[allow(dead_code)]
fn find_all_prime_numbers(n: usize) -> Vec<usize> {

    fn initialize(n: usize) -> Vec<usize> {
        let mut check = Vec::with_capacity(n + 1);
        let mut i = 0;
        while i <= n {
            check.push(0);
            i = i + 1;
        }
        check
    }

    let mut check = initialize(n);
    for i in 2..n + 1 {
        if check[i] == 1 {
            continue;
        }

        let mut j = i + i;
        while j <= n {
            check[j] = 1;
            j = j + i;
        }
    }

    let mut result: Vec<usize> = Vec::new();

    for i in 2..n + 1 {
        if check[i] == 0 {
            result.push(i);
        }
    }

    result
}

/// ------------------------------------

extern crate time;

#[cfg(test)]
mod tests {
    const LOOP_COUNT: i32 = 1000000;

    use super::time;

    #[test]
    fn test_is_prime1() {
        let time = time::get_time();
        let mut count = LOOP_COUNT;
        while count > 0 {
            assert!(super::is_prime1(1) == true);
            assert!(super::is_prime1(2) == true);
            assert!(super::is_prime1(3) == true);
            assert!(super::is_prime1(4) == false);
            assert!(super::is_prime1(5) == true);
            assert!(super::is_prime1(13) == true);

            count = count - 1;
        }
        println!("elpased1: {}", time::get_time() - time);
    }

    #[test]
    fn test_is_prime2() {
        let time = time::get_time();
        let mut count = LOOP_COUNT;
        while count > 0 {
            assert!(super::is_prime2(1) == true);
            assert!(super::is_prime2(2) == true);
            assert!(super::is_prime2(3) == true);
            assert!(super::is_prime2(4) == false);
            assert!(super::is_prime2(5) == true);
            assert!(super::is_prime2(13) == true);

            count = count - 1;
        }
        println!("elpased2: {}", time::get_time() - time);
    }

    #[test]
    fn test_find_all_prime_numbers() {
        let primes = super::find_all_prime_numbers(30);
        assert_eq!(vec![2, 3, 5, 7, 11, 13, 17, 19, 23, 29], primes);
    }
}
