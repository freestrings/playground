use std::mem;

pub fn get_gcd(u: &mut i32, v: &mut i32) -> i32 {
    while *u > 0 {
        if u < v {
            mem::swap(u, v);
        };
        *u = *u - *v;
        println!("gcd1 => u:{}  v:{}", u, v);
    };

    *v
}