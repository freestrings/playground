extern crate serde_json;

use std::{thread, time};
use std::env;
use std::io::Read;

use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct Book {
    category: String,
    author: String,
    title: String,
    isbn: Option<String>,
    price: f64,
}

#[derive(Serialize, Deserialize)]
struct Bicycle {
    color: String,
    price: f64,
}

#[derive(Serialize, Deserialize)]
struct Store {
    book: Vec<Book>,
    bicycle: Bicycle,
}

#[derive(Serialize, Deserialize)]
struct Example {
    store: Store,
    expensive: usize,
}

fn main() {
    let args: Vec<String> = env::args().collect();

    let mut f = std::fs::File::open(&args[1]).unwrap();
    let mut contents = String::new();
    f.read_to_string(&mut contents).expect("파일 읽기 실패");

    for _ in 0..1000 {
        let _: Example = serde_json::from_str(&contents).unwrap();
    }

    let iter = &args[2].parse::<usize>().unwrap();
    thread::sleep(time::Duration::from_secs(1));

    let now = time::Instant::now();
    let json_str = &contents;
    for _ in 0..*iter {
        let _: Example = serde_json::from_str(json_str).unwrap();
    }
    println!("rust: {:?}", now.elapsed().as_millis());

//    loop {
//        thread::sleep(time::Duration::from_micros(10));
//    }
}
