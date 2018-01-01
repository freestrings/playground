use std::mem;
use std::ffi::{CString, CStr};
use std::os::raw::{c_char, c_void};

extern "C" {
    fn hello_as_ptr(ptr: *const u8, len: u32);
    fn hello_into_raw(ptr: *mut c_char, len: u32);
}

#[no_mangle]
pub extern "C" fn callback_str_as_ptr(num: i32, msg: *mut c_char) {
    unsafe {
        let s = CString::from_raw(msg).into_string().unwrap();
        let s = format!("h한글ello: {}, {}", num, s);
        hello_as_ptr(s.as_ptr(), s.len() as u32);
    }
}

#[no_mangle]
pub extern "C" fn callback_str_into_raw(num: i32, msg: *mut c_char) {
    unsafe {
        // let s = CString::from_raw(msg).into_string().unwrap();
        let s = CStr::from_ptr(msg).to_str().unwrap();
        let s = format!("h한글ello: {}, {}", num, s);
        let len = s.len();
        let s = CString::new(s).unwrap();
        hello_into_raw(s.into_raw(), len as u32);
    }
}

#[no_mangle]
pub extern "C" fn multiply(val1: i32, val2: i32) -> i32 {
    val1 * val2
}

#[no_mangle]
pub extern "C" fn append_str(string: *mut c_char) -> *mut c_char {
    unsafe {
        let rust_string = CString::from_raw(string).into_string().unwrap();
        let s = format!("h한글ello: {}", rust_string);
        let s = CString::new(s).unwrap();
        s.into_raw()
    }
}

#[no_mangle]
pub fn append_num(n: u32) -> *mut c_char {
    let s = format!("h한글ello: {}", n);
    let s = CString::new(s).unwrap();
    s.into_raw()
}

#[no_mangle]
pub extern "C" fn alloc(size: usize) -> *mut c_void {
    let mut buf = Vec::with_capacity(size);
    let ptr = buf.as_mut_ptr();
    mem::forget(buf);
    return ptr as *mut c_void;
}

#[no_mangle]
pub extern "C" fn dealloc_str(ptr: *mut c_char) {
    unsafe {
        let _ = CString::from_raw(ptr);
    }
}

#[no_mangle]
pub extern "C" fn dealloc(ptr: *mut c_void, cap: usize) {
    unsafe  {
        let _buf = Vec::from_raw_parts(ptr, 0, cap);
    }
}