#[allow(dead_code)]
pub fn get_gcd(u: &i32, v: &i32) -> i32 {
    if *v <=0 {
        *u
    } else {
        get_gcd(v, &(*u % *v))
    }
}