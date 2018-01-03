#[macro_use]
extern crate stdweb;

fn sum_array(array: Vec<i32>) -> String {
    let mut sum: i64 = 0;
    for v in array {
        sum += v as i64;
    }
    format!("{:?}", sum)
}

fn main() {
    stdweb::initialize();

    js! {
        Module.exports.sumArray = @{sum_array}
    }
}