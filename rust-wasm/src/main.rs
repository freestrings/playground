extern crate emscripten_sys as asm;
extern crate rand;
extern crate sdl2;

use std::f32::consts::PI;
use std::mem;
use std::os::raw::c_void;

use sdl2::EventPump;
use sdl2::event::Event;
use sdl2::keyboard::Keycode;
use sdl2::pixels::Color;
use sdl2::rect::{Point, Rect};
use sdl2::render::{Canvas, Texture, TextureCreator, WindowCanvas};
use sdl2::video::{Window, WindowContext};

fn main() {
    let sdl_context = sdl2::init().unwrap();
    let video_subsystem = sdl_context.video().unwrap();

    let window = video_subsystem.window("Test", 640, 480).build().unwrap();

    let canvas: WindowCanvas = window
        .into_canvas()
        .accelerated()
        .target_texture()
        .build()
        .unwrap();

    let mut event_pump = sdl_context.event_pump().unwrap();

    init(canvas, &mut event_pump);
}

struct Args<'a> {
    canvas: Canvas<Window>,
    events: &'a mut EventPump,
    texture: Texture<'a>,
    points: Vec<Point>,
}

fn init(canvas: Canvas<Window>, events: &mut EventPump) {
    let texture_creator: TextureCreator<WindowContext> = canvas.texture_creator();

    let texture: Texture = texture_creator
        .create_texture_target(texture_creator.default_pixel_format(), 160, 160)
        .unwrap();

    let points = vec![Point::new(1, 0),
                      Point::new(0, 1),
                      Point::new(1, 1),
                      Point::new(2, 1)];

    let mut args = Box::new(Args {
                                canvas: canvas,
                                events: events,
                                texture: texture,
                                points: points,
                            });

    let args_ptr = &mut *args as *mut Args as *mut c_void;

    unsafe {
        asm::emscripten_set_main_loop_arg(Some(main_loop_callback), args_ptr, 0, 1);
    }

    mem::forget(args);
}

extern "C" fn main_loop_callback(arg: *mut c_void) {
    unsafe {
        let mut args: &mut Args = mem::transmute(arg);
        draw(args);
    }
}

enum RotateDirection {
    Left,
    Right,
}

fn rotate(point: &Point, origin: &Point, direction: RotateDirection) -> Point {
    let x = point.x() - origin.x();
    let y = (point.y() - origin.y()) * -1;

    let angle = match direction {
        RotateDirection::Left => PI * 0.5_f32,
        RotateDirection::Right => PI * 1.5_f32,   
    };

    let rotated_x = ((angle.cos() * x as f32 - angle.sin() * y as f32).round() as i32) + origin.x();
    let rotated_y = (((angle.sin() * x as f32 + angle.cos() * y as f32).round() as i32) * -1) +
                    origin.y();

    Point::new(rotated_x, rotated_y)
}

fn draw(args: &mut Args) {
    let canvas = &mut args.canvas;
    let texture = &mut args.texture;
    let events = &mut args.events;

    let origin = Point::new(1, 1);

    let keys: Vec<Event> = events
        .poll_iter()
        .filter(|event| match event {
                    &Event::KeyUp { keycode: Some(Keycode::Up), .. } |
                    &Event::KeyUp { keycode: Some(Keycode::Down), .. } => true,
                    _ => false,
                })
        .collect();

    let key = keys.last();

    let mut points: Vec<Point> = args.points
        .iter()
        .map(|point| match key {
                 Some(&Event::KeyUp { keycode: Some(Keycode::Up), .. }) => {
                     rotate(point, &origin, RotateDirection::Left)
                 }
                 Some(&Event::KeyUp { keycode: Some(Keycode::Down), .. }) => {
                     rotate(point, &origin, RotateDirection::Right)
                 }
                 _ => point.clone(),
             })
        .collect();

    canvas
        .with_texture_canvas(texture, |texture_canvas| {
            texture_canvas.clear();
            texture_canvas.set_draw_color(Color::RGBA(255, 0, 0, 255));
            texture_canvas.draw_points(&points[..]).unwrap();
        })
        .unwrap();

    args.points.truncate(0);
    args.points.append(&mut points);

    canvas.set_draw_color(Color::RGBA(0, 0, 0, 255));

    let src = Some(Rect::new(0, 0, 3, 3));
    let dst = Some(Rect::new(0, 0, 100, 100));

    canvas.clear();
    canvas.copy(&texture, src, dst).unwrap();
    canvas.present();
}
