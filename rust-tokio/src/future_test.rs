extern crate futures_cpupool;
extern crate tokio_timer;

const BIG_PRIME: u64 = 15485867;

use futures::Future;
use futures::Map;

fn is_prime(num: u64) -> bool {

    for i in 2..num {
        if num % i == 0 {
            return false;
        }
    }

    true
}

//
// not yet stable! "impl Trait"
//
// fn add_10<F>(f: F) -> impl Future<Item = i32, Error = F::Error>
//     where F: Future<Item = i32>,
// {
//     f.map(|i| i + 10)
// }

fn add_10<F>(f: F) -> Map<F, fn(i32) -> i32>
    where F: Future<Item = i32>,
{
    fn do_map(i: i32) -> i32 { i + 10 }
    f.map(do_map)
}

#[cfg(test)]
mod tests {

    use super::*;

    use std::time::Duration;

    use futures::prelude::*;
    use futures::future;
    use futures::Future;
    use super::futures_cpupool::CpuPool;
    use super::tokio_timer::Timer;

    #[test]
    fn prime() {
        if is_prime(BIG_PRIME) {
            println!("prime");
        } else {
            println!("no prime");
        }
    }

    #[test]
    fn async_prime() {
        let pool = CpuPool::new_num_cpus();
        let timer = Timer::default();

        let timeout = timer.sleep(Duration::from_millis(750)).then(|_| Err(()));

        let prime_future = pool.spawn_fn(|| {
            Ok(is_prime(BIG_PRIME))
        });

        let winner = timeout.select(prime_future).map(|(win, _)| win);

        match winner.wait() {
            Ok(true) => println!("Prime"),
            Ok(false) => println!("Not prime"),
            Err(_) => println!("Timed out"),
        }
    }

    #[test]
    fn fuse() {
        let mut future = future::ok::<i32, u32>(2);
        assert_eq!(future.poll(), Ok(Async::Ready(2)));
        //assert_eq!(future.poll(), Ok(Async::Ready(2))); => error

        let mut future = future::ok::<i32, u32>(2).fuse();
        assert_eq!(future.poll(), Ok(Async::Ready(2)));
        assert_eq!(future.poll(), Ok(Async::NotReady));
        assert_eq!(future.poll(), Ok(Async::NotReady));
        assert_eq!(future.poll(), Ok(Async::NotReady));
    }

    #[test]
    fn add_10() {
        let mut future = future::ok::<i32, u32>(2);
        match super::add_10(future).poll() {
            Ok(Async::Ready(x)) => assert_eq!(12, x),
            _ => panic!("woops"),
        }
    }
}
