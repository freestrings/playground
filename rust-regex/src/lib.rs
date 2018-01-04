#![feature(test)]

extern crate regex;
extern crate test;

use regex::Captures;
use regex::Regex;

use std::ffi::{CString, CStr};
use std::iter::Iterator;
use std::mem;
use std::os::raw::{c_char, c_void};

fn escape(reg: &Regex, search_value: &str) -> String {
    reg.replace_all(search_value, |caps: &Captures| {
        caps.iter().fold(
            String::new(),
            |mut acc, m| {
                if m.is_some() {
                    acc.push_str(["\\", m.unwrap().as_str()].concat().as_str());
                }
                acc
            }
        )
    }).into_owned()
}

fn __to_string(v: *const c_char) -> String {
    let s = unsafe {
        CStr::from_ptr(v).to_str().unwrap()
    };
    s.to_string()
}

fn __to_ptr(s: String) -> *const c_char {
    let s = CString::new(s).unwrap();
    s.into_raw()
}

#[no_mangle]
pub fn escape_as_regstr(_regstr: *const c_char, _search_value: *const c_char) -> *const c_char {
    let regstr = __to_string(_regstr);
    let search_value = __to_string(_search_value);
    let reg = Regex::new(regstr.as_str()).unwrap();
    let result = escape(&reg, search_value.as_str());
    __to_ptr(result)
}

#[no_mangle]
pub fn alloc(size: usize) -> *mut c_void {
    let mut buf = Vec::with_capacity(size);
    let ptr = buf.as_mut_ptr();
    mem::forget(buf);
    return ptr as *mut c_void;
}

#[no_mangle]
pub fn dealloc_str(ptr: *mut c_char) {
    unsafe {
        let _ = CString::from_raw(ptr);
    }
}

#[cfg(test)]
mod tests {
    use test::Bencher;

    #[test]
    fn escape() {
        let reg = super::Regex::new(r"[\-\[\]{}()*+?.,\\\^$|#\s]").unwrap();

        assert_eq!(super::escape(&reg, "a-b"), "a\\-b");
        assert_eq!(super::escape(&reg, "a[b"), "a\\[b");
        assert_eq!(super::escape(&reg, "a]b"), "a\\]b");
        assert_eq!(super::escape(&reg, "a{b"), "a\\{b");
        assert_eq!(super::escape(&reg, "a}b"), "a\\}b");
        assert_eq!(super::escape(&reg, "a(b"), "a\\(b");
        assert_eq!(super::escape(&reg, "a)b"), "a\\)b");
        assert_eq!(super::escape(&reg, "a*b"), "a\\*b");
        assert_eq!(super::escape(&reg, "a+b"), "a\\+b");
        assert_eq!(super::escape(&reg, "a?b"), "a\\?b");
        assert_eq!(super::escape(&reg, "a.b"), "a\\.b");
        assert_eq!(super::escape(&reg, "a,b"), "a\\,b");
        assert_eq!(super::escape(&reg, "a\\b"), "a\\\\b");
        assert_eq!(super::escape(&reg, "a^b"), "a\\^b");
        assert_eq!(super::escape(&reg, "a$b"), "a\\$b");
        assert_eq!(super::escape(&reg, "a|b"), "a\\|b");
        assert_eq!(super::escape(&reg, "a#b"), "a\\#b");
        assert_eq!(super::escape(&reg, "a\\sb"), "a\\\\sb");
        assert_eq!(super::escape(&reg, "a-[]{}()*+?.,\\^$|#\\s한b"), "a\\-\\[\\]\\{\\}\\(\\)\\*\\+\\?\\.\\,\\\\\\^\\$\\|\\#\\\\s한b");
    }

    #[bench]
    fn bench_escape_10_iter(b: &mut Bencher) {
        b.iter(|| (0..10).for_each(|_| escape()));
    }
}
