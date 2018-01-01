#[macro_use]
extern crate lazy_static;
extern crate regex;

use regex::Regex;

use std::mem;
use std::ffi::CStr;
use std::os::raw::{c_char, c_void};

lazy_static! {
    static ref RE: Regex = Regex::new(r"^bc(d|e)*$").unwrap();
}

#[no_mangle]
pub extern "C" fn pre_compiled_is_match (ptr: *mut c_char) -> bool {
    unsafe {
        let s = CStr::from_ptr(ptr).to_str().unwrap();
        RE.is_match(s)
    }
}

#[no_mangle]
pub extern "C" fn is_match (reg: *mut c_char, ptr: *mut c_char) -> bool {
    unsafe {
        let reg_str = CStr::from_ptr(reg).to_str().unwrap();
        let re: Regex = Regex::new(reg_str).unwrap();
        let s = CStr::from_ptr(ptr).to_str().unwrap();
        re.is_match(s)
    }
}

#[no_mangle]
pub extern "C" fn inline_is_match(iter: u32, ptr: *mut c_char) -> usize {
    let s = unsafe { CStr::from_ptr(ptr).to_str().unwrap() };
    let words: Vec<&str> = s.split(",").collect();
    let words = &words[..];
    let len = words.len();

    let mut matched = 0;
    for _ in 0..iter {
        for s in 0..len {
            if RE.is_match(words[s]) == true {
                matched += 1;
            }
        }
    }
    matched
}

#[no_mangle]
pub extern "C" fn sum_in_rust(num: i32) -> i32 {
    let mut sum = 0;
    for i in 0..num {
        sum += i;
    }
    sum
}

#[no_mangle]
pub extern "C" fn inline_sum_in_rust(iter: i32, num: i32) -> i32 {
    let mut last_sum = 0;
    for _ in 0..iter {
        let mut sum = 0;
        for i in 0..num {
            sum += i;
        }
        last_sum = last_sum.max(sum);
    }
    last_sum
}

#[no_mangle]
pub extern "C" fn alloc(size: usize) -> *mut c_void {
    let mut buf = Vec::with_capacity(size);
    let ptr = buf.as_mut_ptr();
    mem::forget(buf);
    return ptr as *mut c_void;
}

#[cfg(test)]
mod tests {

    use super::regex::Regex;

    #[test]
    fn a() {
        let re = Regex::new(r"^bc(d|e)*$").unwrap();
        println!("{}", re.is_match("bce"));
    }

    #[test]
    fn b() {
        let split = "a b c d".split(" ");
        for s in split {
            println!("{}", s);
        }
    }
}