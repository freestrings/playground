extern crate clap;
extern crate image;
extern crate imageproc;
extern crate simplemad;

use clap::App;
use std::f64;
use std::fs::File;

use image::{Rgb, RgbImage};
use imageproc::{drawing, rect};
use simplemad::{Decoder, MadFixed32};

///https://en.wikipedia.org/wiki/Root_mean_square
///http://m.blog.naver.com/pkw00/220226903866
fn to_rms(samples: &Vec<MadFixed32>) -> f64 {
    let sum = samples.iter().fold(0.0, |_, sample| (sample.to_raw() as f64).powi(2));
    let len = samples.len() as f64;
    (sum / len).sqrt()
}

fn find_max(values: Vec<f64>) -> f64 {
    let mut max = f64::MIN;
    for v in values.iter() {
        if max.max(*v) == *v {
            max = *v;
        }
    }
    max
}

fn samples(file: File) -> Vec<[f64; 2]> {
    Decoder::decode(file)
        .unwrap()
        .filter_map(|r| match r {
            Ok(f) => Some(f),
            _ => None,
        })
        .map(|f| [to_rms(&f.samples[0]), to_rms(&f.samples[1])])
        .collect()
}

fn main() {
    let matches = App::new("waveform")
        .version("0.1")
        .author("Changseok Han <freestrings@gmail.com>")
        .args_from_usage("<INPUT>... 'mp3 file pathes. ex) ./waveform file1 file2'")
        .get_matches();

    let pathes:Vec<_> = matches.values_of("INPUT").unwrap().collect();

    for path in pathes.iter() {

        match File::open(path) {
            Ok(file) => {
                let samples: Vec<[f64; 2]> = samples(file);

                let left_max = find_max(samples.iter().map(|s| s[0]).collect());
                let right_max = find_max(samples.iter().map(|s| s[1]).collect());

                let white = Rgb([255u8, 255u8, 255u8]);

                let image_height: usize = 120;
                let image_width: usize = 512;
                let half_height = image_height / 2;

                let mut image = RgbImage::new(image_width as u32, image_height as u32);

                let len = samples.len();
                let ih = image_height as f64;

                for n in 0..len {

                    let left_sample = samples[n][0];
                    let right_sample = samples[n][1];

                    let half_y = left_sample * ih / left_max / 2.0;

                    let x = (image_width * n / len) as i32;
                    let y = (half_height as f64 - half_y) as i32;
                    let w = 1 as u32;
                    let h = (half_y * 2.0) as u32;

                    let h = if h <= 0 { 1 } else { h };

                    drawing::draw_hollow_rect_mut(&mut image,
                                                  rect::Rect::at(x, y).of_size(w, h),
                                                  white);

                }

                match image.save(format!("{}.png", path)) {
                    Ok(_) => println!("done: {}", path),
                    Err(e) => println!("err: {:?}", e),
                };

            }

            Err(e) => println!("Cannot open file {}", e),
        }
    }

}
