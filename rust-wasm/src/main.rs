extern crate emscripten_sys as asm;
extern crate rand;
extern crate sdl2;

use std::f32::consts::PI;
use std::mem;
use std::os::raw::c_void;

use rand::distributions::{IndependentSample, Range};

use sdl2::EventPump;
use sdl2::event::Event;
use sdl2::keyboard::Keycode;
use sdl2::pixels::Color;
use sdl2::rect::{Point, Rect};
use sdl2::render::{Canvas, Texture, TextureCreator, WindowCanvas};
use sdl2::video::{Window, WindowContext};

const COLUMNS: u32 = 10;
const ROWS: u32 = 20;
const SCALE: u32 = 20;
const DEFAULT_GRAVITY: u8 = 20;

//
//    #
// #, @, #
const BLOCK_T: &[(u8, u8)] = &[(1, 0), (0, 1), (1, 1), (2, 1)];
//
//    #
//    @
// #, #
const BLOCK_J: &[(u8, u8)] = &[(0, 2), (1, 2), (1, 1), (1, 0)];
//
// #
// @
// #, #
const BLOCK_L: &[(u8, u8)] = &[(1, 2), (0, 2), (0, 1), (0, 0)];
//
//    #, #
// #, @
const BLOCK_S: &[(u8, u8)] = &[(2, 0), (1, 0), (1, 1), (0, 1)];
//
// #, #
//    @, #
const BLOCK_Z: &[(u8, u8)] = &[(0, 0), (1, 0), (1, 1), (2, 1)];
//
// #, #
// #, #
const BLOCK_O: &[(u8, u8)] = &[(0, 0), (1, 0), (0, 1), (1, 1)];
//
// #, #, #, #
const BLOCK_I: &[(u8, u8)] = &[(0, 0), (1, 0), (2, 0), (3, 0)];

const BLOCKS: [&[(u8, u8)]; 7] = [
    BLOCK_T,
    BLOCK_J,
    BLOCK_L,
    BLOCK_S,
    BLOCK_Z,
    BLOCK_O,
    BLOCK_I,
];

const COLORS: [(u8, u8, u8); 7] = [
    (128, 0, 128),
    (0, 0, 255),
    (255, 165, 0),
    (128, 255, 0),
    (255, 0, 0),
    (255, 255, 0),
    (0, 255, 255),
];

fn main() {
    let sdl_context = sdl2::init().unwrap();
    let events = sdl_context.event_pump().unwrap();
    let video_subsystem = sdl_context.video().unwrap();
    let window = video_subsystem
        .window("Test", COLUMNS * SCALE, ROWS * SCALE)
        .build()
        .unwrap();
    let canvas: WindowCanvas = window
        .into_canvas()
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

extern "C" fn main_loop_callback(arg: *mut c_void) {
    unsafe {
        let mut app: &mut App = mem::transmute(arg);
        app.draw();
    }
}

struct Gravity {
    counter: i32,
    amount: u8,
}

impl Gravity {
    fn new(amount: u8) -> Gravity {
        Gravity {
            counter: 0,
            amount: amount,
        }
    }

    fn compute(&mut self, points: &Vec<Point>, range: Rect) -> Option<Vec<Point>> {
        self.counter += 1;

        if range.y() + range.height() as i32 >= ROWS as i32 {
            return None;
        }

        if self.counter > self.amount as i32 {
            self.counter = 0;

            Some(
                points
                    .iter()
                    .map(|point| Point::new(point.x(), point.y() + 1))
                    .collect(),
            )
        } else {
            None
        }
    }
}

struct BlockHandler {
    points: Vec<Point>,
}

impl BlockHandler {
    fn new(points: Vec<Point>) -> BlockHandler {
        BlockHandler { points: points }
    }

    fn next_block_index() -> u8 {
        let mut rng = rand::thread_rng();
        let between = Range::new(0, 7);
        between.ind_sample(&mut rng)
    }

    fn get_points(&self) -> Vec<Point> {
        self.points.clone()
    }

    fn handle(&mut self, events: &mut EventPump) {
        for event in events.poll_iter() {
            match event {
                Event::KeyDown { keycode: Some(Keycode::Up), .. } => self.rotate(),
                Event::KeyDown { keycode: Some(Keycode::Left), .. } => self.move_left(),
                Event::KeyDown { keycode: Some(Keycode::Right), .. } => self.move_right(),
                Event::KeyDown { keycode: Some(Keycode::Down), .. } => self.move_down(),
                _ => (),
            }

            let range = self.range();

            if range.x() < 0 {
                self.shift(|| (range.x().abs(), 0));
            }

            let right = range.x() + range.width() as i32;
            if right >= COLUMNS as i32 {
                self.shift(|| (COLUMNS as i32 - right, 0));
            }

            let bottom = range.y() + range.height() as i32;
            if bottom >= ROWS as i32 {
                self.shift(|| (0, ROWS as i32 - bottom));
            }
        }
    }

    //
    // https://www.youtube.com/watch?v=Atlr5vvdchY
    //
    fn rotate(&mut self) {
        let angle = PI * 1.5_f32;

        let center = self.center_point();

        let mut points = self.points
            .iter()
            .map(|point| {
                let x = point.x() - center.x();
                let y = point.y() - center.y();
                let y = y * -1;

                let rotated_x = angle.cos() * x as f32 - angle.sin() * y as f32;
                let rotated_x = rotated_x.round() as i32 + center.x();
                let rotated_y = angle.sin() * x as f32 + angle.cos() * y as f32;
                let rotated_y = rotated_y.round() as i32 * -1 + center.y();

                Point::new(rotated_x, rotated_y)
            })
            .collect();

        self.replace(&mut points);
    }

    fn shift<F>(&mut self, mut f: F)
    where
        F: FnMut() -> (i32, i32),
    {
        let mut points = self.points
            .iter()
            .map(|point| {
                let raw_point = f();
                Point::new(point.x() + raw_point.0, point.y() + raw_point.1)
            })
            .collect();

        self.replace(&mut points);
    }

    fn move_left(&mut self) {
        self.shift(|| (-1, 0));
    }

    fn move_right(&mut self) {
        self.shift(|| (1, 0));
    }

    fn move_down(&mut self) {
        self.shift(|| (0, 1));
    }

    fn center_point(&mut self) -> Point {
        Point::new(self.points[2].x(), self.points[2].y())
    }

    fn range(&self) -> Rect {

        let mut min_x = i32::max_value();
        let mut max_x = i32::min_value();
        let mut min_y = i32::max_value();
        let mut max_y = i32::min_value();

        let points = &self.points;
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

        Rect::new(
            min_x,
            min_y,
            (max_x - min_x).abs() as u32 + 1,
            (max_y - min_y).abs() as u32 + 1,
        )
    }

    fn replace(&mut self, target_points: &mut Vec<Point>) {
        self.points.truncate(0);
        self.points.append(target_points);
    }
}

struct Block {
    // index: u8,
    points: Vec<Point>,
    color: Color,
    is_freeze: bool,
}

impl Block {
    fn new(index: u8) -> Block {

        let points: Vec<Point> = BLOCKS[index as usize]
            .iter()
            .map(|raw_point| {
                Point::new(raw_point.0 as i32, raw_point.1 as i32)
            })
            .collect();

        let (r, g, b) = COLORS[index as usize];
        let color = Color::RGB(r, g, b);

        Block {
            // index: index,
            points: points,
            color: color,
            is_freeze: false,
        }
    }

    // fn index(&self) -> u8 {
    //     self.index
    // }

    fn points(&self) -> Vec<Point> {
        self.points.clone()
    }

    fn color(&self) -> Color {
        self.color.clone()
    }

    fn update(&mut self, points: &mut Vec<Point>) {
        let block_handler = BlockHandler::new(points.clone());
        let range = block_handler.range();

        if range.y() + range.height() as i32 >= ROWS as i32 {
            self.is_freeze = true;
        }

        self.points.truncate(0);
        self.points.append(points);
    }

    fn is_freezed(&self) -> bool {
        self.is_freeze == true
    }
}

struct App<'a> {
    canvas: Canvas<Window>,
    events: EventPump,
    texture: Texture<'a>,
    block: Block,
    gravity: Gravity,
}

impl<'a> App<'a> {
    fn new(
        canvas: WindowCanvas,
        events: EventPump,
        texture_creator: &'a TextureCreator<WindowContext>,
    ) -> App {

        let texture = texture_creator
            .create_texture_target(None, COLUMNS, ROWS)
            .unwrap();

        App {
            canvas: canvas,
            events: events,
            texture: texture,
            block: Block::new(BlockHandler::next_block_index()),
            gravity: Gravity::new(DEFAULT_GRAVITY),
        }
    }

    fn apply_gravity_to_current_block(&mut self) {
        if self.block.is_freezed() {
            return;
        }

        let block_handler = BlockHandler::new(self.block.points().clone());

        let computed = self.gravity.compute(
            &self.block.points,
            block_handler.range(),
        );

        if let Some(mut points) = computed {
            self.block.update(&mut points);
        }
    }

    fn block_move_by_event(&mut self) {
        if self.block.is_freezed() {
            return;
        }

        let events = &mut self.events;
        let mut block_handler = BlockHandler::new(self.block.points().clone());
        block_handler.handle(events);
        self.block.update(&mut block_handler.get_points());
    }

    fn draw_background(&mut self) {
        self.canvas.set_draw_color(Color::RGB(0, 0, 0));
        self.canvas.clear();
    }

    fn render(&mut self) {
        self.canvas.present();
    }

    fn draw_current_block(&mut self) {
        let canvas = &mut self.canvas;
        let texture = &mut self.texture;

        let points = &self.block.points();
        let color = self.block.color();

        for point in points.iter() {
            let src = Some(Rect::new(point.x(), point.y(), 1, 1));
            let dst = Some(Rect::new(
                point.x() * SCALE as i32 + 1,
                point.y() * SCALE as i32 + 1,
                SCALE - 2,
                SCALE - 2,
            ));

            canvas
                .with_texture_canvas(texture, |texture_canvas| {
                    texture_canvas.clear();
                    texture_canvas.set_draw_color(color);
                    texture_canvas
                        .draw_point(Point::new(point.x(), point.y()))
                        .unwrap();
                })
                .unwrap();

            canvas.copy(&texture, src, dst).unwrap();
        }
    }

    fn draw(&mut self) {
        self.apply_gravity_to_current_block();
        self.block_move_by_event();
        self.draw_background();
        self.draw_current_block();
        self.render();
    }
}
