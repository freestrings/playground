use std::mem;

#[allow(dead_code)]
pub fn get_gcd(u: &mut i32, v: &mut i32) -> i32 {
    while *v > 0 {
        *u = *u % *v;
        mem::swap(u, v);
        println!("gcd2 => u:{}  v:{}", u, v);
    }

    *u
}