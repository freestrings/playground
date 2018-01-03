#![feature(proc_macro)]

extern crate wasm_bindgen;

use wasm_bindgen::prelude::*;

wasm_bindgen! {
    pub fn sum(num: u32) -> String {
        let mut s: i64 = 0;
        for i in 0..num {
            s += i as i64;
        }
        format!("{}", s)
    }

    pub fn sum_all(iter: u32, num: u32) -> String {
        let mut s: i64 = 0;
        for _ in 0..iter {
            for i in 0..num {
                s += i as i64;
            }
        }

        format!("{}", s)
    }

}
