#[macro_use]
extern crate clap;
extern crate image;
extern crate imageproc;
extern crate simplemad;
extern crate term;

use std::f64;
use std::i64;
use std::fs::File;

use std::error::Error;

use clap::App;
use image::{Rgb, RgbImage};
use imageproc::{drawing, rect};
use simplemad::{Decoder, MadFixed32};

///https://en.wikipedia.org/wiki/Root_mean_square
///http://m.blog.naver.com/pkw00/220226903866
fn into_rms(samples: &Vec<MadFixed32>) -> f64 {
    let sum = samples.iter().fold(0.0, |_, sample| (sample.to_raw() as f64).powi(2));
    let len = samples.len() as f64;
    (sum / len).sqrt()
}

fn samples(file: File) -> Vec<f64> {
    Decoder::decode(file)
        .unwrap()
        .filter_map(|r| match r {
            Ok(f) => Some(f),
            _ => None,
        })
        .map(|f| [into_rms(&f.samples[0]), into_rms(&f.samples[1])]) //rms
        .map(|s| (s[0] + s[1]) / 2.0) //mono
        .collect()
}

fn hexadecimal_to_decimal(hex: &str) -> u8 {
    match i64::from_str_radix(hex, 16) {
        Ok(v) => v as u8,
        _ => 0,
    }
}

fn hexcolor_to_rgb(hex: &str) -> Rgb<u8> {
    let hex = if hex.starts_with("#") { &hex[1..] } else { hex };
    let r = hexadecimal_to_decimal(&hex[0..2]);
    let g = hexadecimal_to_decimal(&hex[2..4]);
    let b = hexadecimal_to_decimal(&hex[4..6]);
    Rgb([r, g, b])
}

fn main() {

    let matches = App::new("waveform")
        .version("0.1")
        .author("Changseok Han <freestrings@gmail.com>")
        .args_from_usage(concat!("<INPUT>... 'mp3 file pathes. ex) ./waveform file1 file2'\n",
                                 // width
                                 "-w --width=[WIDTH] 'optional) a image width. the default \
                                  width is 512 pixel'\n",
                                 // height
                                 "-h --height=[HEIGTH] 'optional) a image height. the default \
                                  height is 120 pixel'\n",
                                 // background color
                                 "-b --background=[BACKGROUND] 'optional) a background hex \
                                  color. the default value is #000000'\n",
                                 // foreground color
                                 "-f --foreground=[FOREGROUND] 'optional) a foreground hex \
                                  color. the default value is #ffffff'\n",
                                 "-o --output",
                                 // verbose
                                 "-v --verbose"))
        .get_matches();

    let mut terminal = term::stdout().unwrap();

    let image_width = value_t!(matches.value_of("width"), u32).unwrap_or(512 as u32);
    let image_height = value_t!(matches.value_of("height"), u32).unwrap_or(120 as u32);
    //
    // the color of canvas
    //
    let background = hexcolor_to_rgb(matches.value_of("background").unwrap_or("#000000"));
    //
    // the color of wave
    //
    let foreground = hexcolor_to_rgb(matches.value_of("foreground").unwrap_or("#ffffff"));

    let pathes: Vec<_> = matches.values_of("INPUT").unwrap().collect();

    let mut count = 0;
    let total_pathes = pathes.len();

    for path in pathes {

        count += 1;

        match File::open(path) {
            Ok(file) => {
                let samples = samples(file);
                let sample_len = samples.len();
                let max_sample = samples.iter().cloned().fold(f64::MIN, f64::max);

                let mut image = RgbImage::new(image_width, image_height);

                drawing::draw_filled_rect_mut(&mut image,
                                              rect::Rect::at(0 as i32, 0 as i32)
                                                  .of_size(image_width as u32,
                                                           image_height as u32),
                                              background);

                for n in 0..sample_len {
                    let half_height = samples[n] * image_height as f64 / max_sample / 2_f64;
                    let x = (image_width as usize * n / sample_len) as i32;
                    let y = (image_height as f64 / 2_f64 - half_height) as i32;
                    let h = (half_height * 2_f64) as u32;
                    let h = if h <= 0 { 1 } else { h };

                    drawing::draw_hollow_rect_mut(&mut image,
                                                  rect::Rect::at(x, y).of_size(1_u32, h),
                                                  foreground);

                }

                let result = image.save(format!("{}.png", path));
                if matches.is_present("verbose") {
                    match result {
                        Ok(_) => {
                            terminal.fg(term::color::YELLOW).unwrap();
                            print!("Done ");
                            terminal.reset().unwrap();
                            terminal.fg(term::color::GREEN).unwrap();
                            print!("{}/{}", count, total_pathes);
                            terminal.reset().unwrap();
                            println!(" \"{}\"", path);
                        }
                        Err(e) => {
                            terminal.fg(term::color::RED).unwrap();
                            print!("Err ");
                            terminal.reset().unwrap();
                            terminal.fg(term::color::GREEN).unwrap();
                            print!("{}/{}", count, total_pathes);
                            terminal.reset().unwrap();
                            println!("{}(\"{}\")", e.description(), path);
                        }
                    }
                }
            }

            Err(e) => {
                terminal.fg(term::color::RED).unwrap();
                print!("Cannot open ");
                terminal.fg(term::color::GREEN).unwrap();
                print!("{}/{} ", count, total_pathes);
                terminal.reset().unwrap();
                println!("{}(\"{}\")", e.description(), path);
            }
        }
    }

}


#[cfg(test)]
mod tests {

    use std::f64;
    use std::i64;

    #[test]
    fn max() {
        let vector = vec![0f64, 1f64, 2f64];
        let max = vector.iter().cloned().fold(f64::MIN, f64::max);
        assert_eq!(max, 2f64);
    }

    #[test]
    fn hex_to_num() {
        assert_eq!(i64::from_str_radix("ff", 16).unwrap(), 255);
        assert_eq!(i64::from_str_radix("00", 16).unwrap(), 0);
        assert_eq!(i64::from_str_radix("01", 16).unwrap(), 1);
    }
}