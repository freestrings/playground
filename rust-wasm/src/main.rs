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

    let events = sdl_context.event_pump().unwrap();

    let video_subsystem = sdl_context.video().unwrap();
    let window = video_subsystem.window("Test", 640, 480).build().unwrap();
    let canvas: WindowCanvas = window.into_canvas()
        .accelerated()
        .target_texture()
        .build()
        .unwrap();

    let texture_creator: TextureCreator<WindowContext> = canvas.texture_creator();

    let mut app = Box::new(App::new(canvas, events, &texture_creator));

    let app_ptr = &mut *app as *mut App as *mut c_void;

    unsafe {
        asm::emscripten_set_main_loop_arg(Some(main_loop_callback), app_ptr, 0, 1);
    }

    mem::forget(app);
}

//
//    #
// #, #, #
//
const BLOCK_TYPE1: &[(u8, u8)] = &[(1, 0), (0, 1), (1, 1), (2, 1)];

struct KeyHandler {
    //
    // Key Up을 이벤트 루프에서 읽다보니 속도가 빨라서 블럭 회전이 너무 많이 된다. keyup으로 토글해 주자.
    //
    up_pressed: bool,
}

impl KeyHandler {
    fn new() -> KeyHandler {
        KeyHandler { up_pressed: false }
    }

    fn get_keycodes(&mut self, events: &mut EventPump) -> Vec<Keycode> {
        let mut key_events = vec![];

        for event in events.poll_iter() {
            match event {
                Event::KeyDown { keycode: Some(Keycode::Up), .. } => {
                    if self.up_pressed == false {
                        key_events.push(Keycode::Up);
                    }

                    self.up_pressed = true;
                }
                Event::KeyUp { keycode: Some(Keycode::Up), .. } => {
                    self.up_pressed = false;
                }
                Event::KeyDown { keycode: Some(Keycode::Left), .. } => {
                    key_events.push(Keycode::Left);
                }
                Event::KeyDown { keycode: Some(Keycode::Right), .. } => {
                    key_events.push(Keycode::Right);
                }
                _ => (),
            }
        }

        key_events
    }
}

struct App<'a> {
    canvas: Canvas<Window>,
    events: EventPump,
    texture: Texture<'a>,
    current_block: Vec<Point>,
    key_handler: KeyHandler,
}

impl<'a> App<'a> {
    fn new(canvas: WindowCanvas,
           events: EventPump,
           texture_creator: &'a TextureCreator<WindowContext>)
           -> App {

        let texture = texture_creator.create_texture_target(None, 10, 10).unwrap();

        App {
            canvas: canvas,
            events: events,
            texture: texture,
            current_block: BLOCK_TYPE1.iter()
                .map(|tut| Point::new(tut.0 as i32, tut.1 as i32))
                .collect(),
            key_handler: KeyHandler::new(),
        }
    }

    //
    // https://www.youtube.com/watch?v=Atlr5vvdchY
    //
    fn block_rotate(point: &Point) -> Point {
        // the center of rotation
        let origin = Point::new(1, 1);

        let x = point.x() - origin.x();
        let y = (point.y() - origin.y()) * -1;

        let angle = PI * 1.5_f32;
        let rotated_x = ((angle.cos() * x as f32 - angle.sin() * y as f32).round() as i32) +
                        origin.x();
        let rotated_y = (((angle.sin() * x as f32 + angle.cos() * y as f32).round() as i32) *
                         -1) + origin.y();

        Point::new(rotated_x, rotated_y)
    }

    fn block_move_left(point: &Point) -> Point {
        Point::new(point.x - 1, point.y)
    }

    fn block_move_right(point: &Point) -> Point {
        Point::new(point.x + 1, point.y)
    }

    fn block_range(points: &Vec<Point>) -> Rect {
        
        let mut min_x = i32::max_value();
        let mut max_x = i32::min_value();
        let mut min_y = i32::max_value();
        let mut max_y = i32::min_value();

        for b in points {
            if b.x.gt(&max_x) {
                max_x = b.x;
            }
            if b.x.lt(&min_x) {
                min_x = b.x;
            }
            if b.y.gt(&max_y) {
                max_y = b.y;
            }
            if b.y.lt(&min_y) {
                min_y = b.y;
            }
        }

        Rect::new(min_x, min_y, (max_x - min_x).abs() as u32, (max_y - min_y).abs() as u32)
    }

    fn block_move(&mut self) {
        let events = &mut self.events;
        let block_rotator = &mut self.key_handler;

        for key in block_rotator.get_keycodes(events) {
            let mut points = match key {
                Keycode::Up => {
                    self.current_block.iter()
                        .map(|point| Self::block_rotate(point))
                        .collect()
                }
                Keycode::Left => {
                    self.current_block.iter()
                        .map(|point| Self::block_move_left(point))
                        .collect()
                }
                Keycode::Right => {
                    self.current_block.iter()
                        .map(|point| Self::block_move_right(point))
                        .collect()
                }
                _ => {
                    self.current_block.clone()
                }
            };

            println!("{:?}", points);

            self.current_block.truncate(0);
            self.current_block.append(&mut points);
        }
    }

    fn draw(&mut self) {
        self.block_move();

        let canvas = &mut self.canvas;
        let texture = &mut self.texture;
        let points = &mut self.current_block;

        canvas.with_texture_canvas(texture, |texture_canvas| {
                texture_canvas.clear();
                texture_canvas.set_draw_color(Color::RGBA(255, 0, 0, 255));
                texture_canvas.draw_points(&points[..]).unwrap();
            })
            .unwrap();

        canvas.set_draw_color(Color::RGBA(0, 0, 0, 255));

        let src = Some(Self::block_range(points));
        let dst = Some(Rect::new(0, 0, 100, 100));

        canvas.clear();
        canvas.copy(&texture, src, dst).unwrap();
        canvas.present();
    }
}



extern "C" fn main_loop_callback(arg: *mut c_void) {
    unsafe {
        let mut app: &mut App = mem::transmute(arg);
        app.draw();
    }
}
