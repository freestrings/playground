extern crate simplemad;

fn main() {}

#[cfg(test)]
mod tests {

    use std::path::Path;
    use std::fs::File;

    use simplemad::{Decoder, Frame, MadFixed32};

    #[test]
    fn t1() {
        let path = Path::new("resources/test.mp3");
        let file = File::open(&path).unwrap();
        let decoder = Decoder::decode(file).unwrap();

        let samples: Vec<Vec<f64>> = decoder.filter_map(|r| match r {
            Ok(f) => Some(f), 
            _ => None
        })
        .map(|f| {
            let samples = f.samples;

            let mut l_sum = 0_f64;
            for l_sample in &samples[0] {
                l_sum += l_sample.to_f64();
            }

            let mut r_sum = 0_f64;

            vec![l_sum, r_sum]
        })
        .collect();

        for sample in &samples {
            println!("{}", sample[0]);
        }
    }
}