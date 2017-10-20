extern crate futures_cpupool;
extern crate tokio_timer;

const BIG_PRIME: u64 = 15485867;

fn is_prime(num: u64) -> bool {

    for i in 2..num {
        if num % i == 0 {
            return false;
        }
    }

    true
}


#[cfg(test)]
mod tests {

    use super::*;

    use std::time::Duration;

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
}
