extern crate simplemad;

fn main() {}

#[cfg(test)]
mod tests {

    use std::path::Path;
    use std::fs::File;

    use simplemad::{Decoder, Frame, MadFixed32};

    ///
    ///https://en.wikipedia.org/wiki/Root_mean_square
    ///http://m.blog.naver.com/pkw00/220226903866
    ///
    fn to_rms(samples: &Vec<MadFixed32>) -> f64 {
        let mut sum = 0_f64;

        for sample in samples {
            sum += sample.to_f64().powf(2.0);
        }

        (sum / (samples.len() as f64)).sqrt()
    }

    #[test]
    fn t1() {
        let path = Path::new("resources/test.mp3");
        let file = File::open(&path).unwrap();
        let decoder = Decoder::decode(file).unwrap();

        let samples: Vec<Vec<f64>> = decoder.filter_map(|r| match r {
                Ok(f) => Some(f),
                _ => None,
            })
            .map(|f| vec![to_rms(&f.samples[0]), to_rms(&f.samples[1])])
            .collect();

        for sample in &samples {
            println!("{}", sample[0]);
        }
    }
}