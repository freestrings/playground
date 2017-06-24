extern crate emscripten_sys as asm;
extern crate rand;
extern crate sdl2;
#[macro_use]
extern crate lazy_static;

use std::cell::RefCell;
use std::f32::consts::PI;
use std::mem;
use std::os::raw::c_void;
use std::rc::Rc;
use std::collections::HashSet;
use std::sync::Mutex;

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
const BORDER: u32 = 1;
const WINDOW_WIDTH: u32 = BORDER + COLUMNS + BORDER + RIGHT_PANEL + BORDER;
const WINDOW_HEIGHT: u32 = BORDER + ROWS + BORDER;
const RIGHT_PANEL: u32 = 5;
const SCALE: u32 = 20;
const DEFAULT_GRAVITY: u8 = 20;

//
//    #
// #, @, #
const BLOCK_T: &[(u8, u8)] = &[(1, 0), (0, 1), (1, 1), (2, 1)];
//
// #
// #, @, #
const BLOCK_J: &[(u8, u8)] = &[(0, 0), (0, 1), (1, 1), (2, 1)];
//
//       #
// #, @, #
const BLOCK_L: &[(u8, u8)] = &[(2, 0), (2, 1), (1, 1), (0, 1)];
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
// #, #, @, #
const BLOCK_I: &[(u8, u8)] = &[(0, 0), (1, 0), (2, 0), (3, 0)];

const COLOR_PURPLE: (u8, u8, u8) = (128, 0, 128);
const COLOR_BLUE: (u8, u8, u8) = (0, 0, 255);
const COLOR_ORANGE: (u8, u8, u8) = (255, 165, 0);
const COLOR_LIME: (u8, u8, u8) = (128, 255, 0);
const COLOR_RED: (u8, u8, u8) = (255, 0, 0);
const COLOR_YELLOW: (u8, u8, u8) = (255, 255, 0);
const COLOR_CYAN: (u8, u8, u8) = (0, 255, 255);
const COLOR_BLACK: (u8, u8, u8) = (0, 0, 0);

lazy_static! {
    static ref EVENT_Q: Mutex<Vec<BlockEvent>> = Mutex::new(vec![]);
}

fn _move(block_event: BlockEvent) -> u8 {
    match EVENT_Q.lock() {
        Ok(mut v) => {
            v.push(block_event);
            0
        }
        Err(_) => 1,
    }
}

#[no_mangle]
pub fn move_left() -> u8 {
    println!("click left");
    _move(BlockEvent::Left)
}

#[no_mangle]
pub fn move_right() -> u8 {
    println!("click right");
    _move(BlockEvent::Right)
}

#[no_mangle]
pub fn move_down() -> u8 {
    println!("click down");
    _move(BlockEvent::Down)
}

#[no_mangle]
pub fn move_rotate() -> u8 {
    println!("click rotate");
    _move(BlockEvent::Rotate)
}

#[no_mangle]
pub fn move_drop() -> u8 {
    println!("click drop");
    _move(BlockEvent::Drop)
}

fn main() {
    let sdl_context = sdl2::init().unwrap();
    let events = sdl_context.event_pump().unwrap();
    let video_subsystem = sdl_context.video().unwrap();
    let window = video_subsystem
        .window("Test", WINDOW_WIDTH * SCALE, WINDOW_HEIGHT * SCALE)
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
        app.run();
    }
}

type Points = Vec<Point>;

#[derive(Clone, Debug, PartialEq)]
enum BlockType {
    T,
    J,
    L,
    S,
    Z,
    O,
    I,
}

impl BlockType {
    fn new(index: u8) -> BlockType {
        match index {
            1 => BlockType::T,
            2 => BlockType::J,
            3 => BlockType::L,
            4 => BlockType::S,
            5 => BlockType::Z,
            6 => BlockType::O,
            7 => BlockType::I,
            _ => BlockType::T,
        }
    }

    fn random() -> BlockType {
        let mut rng = rand::thread_rng();
        let between = Range::new(1, 8);
        BlockType::new(between.ind_sample(&mut rng))
    }

    fn index(&self) -> u8 {
        match *self {
            BlockType::T => 1,
            BlockType::J => 2,
            BlockType::L => 3,
            BlockType::S => 4,
            BlockType::Z => 5,
            BlockType::O => 6,
            BlockType::I => 7,
        }
    }

    fn color(&self) -> Color {
        let (r, g, b) = match *self {
            BlockType::T => COLOR_PURPLE,
            BlockType::J => COLOR_BLUE,
            BlockType::L => COLOR_ORANGE,
            BlockType::S => COLOR_LIME,
            BlockType::Z => COLOR_RED,
            BlockType::O => COLOR_YELLOW,
            BlockType::I => COLOR_CYAN,
        };

        Color::RGB(r, g, b)
    }

    fn points(&self) -> Points {
        match *self {
            BlockType::T => BLOCK_T,
            BlockType::J => BLOCK_J,
            BlockType::L => BLOCK_L,
            BlockType::S => BLOCK_S,
            BlockType::Z => BLOCK_Z,
            BlockType::O => BLOCK_O,
            BlockType::I => BLOCK_I,
        }.iter()
            .map(|raw_point| {
                Point::new(raw_point.0 as i32, raw_point.1 as i32)
            })
            .collect()
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

    fn compute(&mut self, points: &Points, range: Rect) -> Option<Points> {
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

trait PointTransformer {
    fn get_points_ref(&mut self) -> &Points;

    fn replace(&mut self, points: &mut Points);

    //
    // https://www.youtube.com/watch?v=Atlr5vvdchY
    //
    fn rotate(&mut self) {
        let angle = PI * 0.5_f32;
        let center = Point::new(self.get_points_ref()[2].x(), self.get_points_ref()[2].y());
        let mut points = self.get_points_ref()
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
        let mut points = self.get_points_ref()
            .iter()
            .map(|point| {
                let raw_point = f();
                Point::new(point.x() + raw_point.0, point.y() + raw_point.1)
            })
            .collect();

        self.replace(&mut points);
    }

    fn move_left<GARD>(&mut self, rollback_gard: GARD)
    where
        GARD: Fn(&Points) -> bool,
    {
        self.shift(|| (-1, 0));
        if rollback_gard(self.get_points_ref()) {
            self.shift(|| (1, 0));
        }
    }

    fn move_right<GARD>(&mut self, rollback_gard: GARD)
    where
        GARD: Fn(&Points) -> bool,
    {
        self.shift(|| (1, 0));
        if rollback_gard(self.get_points_ref()) {
            self.shift(|| (-1, 0));
        }
    }

    fn move_down<GARD>(&mut self, rollback_gard: GARD)
    where
        GARD: Fn(&Points) -> bool,
    {
        self.shift(|| (0, 1));
        if rollback_gard(self.get_points_ref()) {
            self.shift(|| (0, -1));
        }
    }

    fn drop_down<GARD>(&mut self, rollback_gard: GARD)
    where
        GARD: Fn(&Points) -> bool,
    {
        let range = self.range();
        let start_y = range.y() + range.height() as i32;
        for _ in start_y..ROWS as i32 {
            self.shift(|| (0, 1));
            if rollback_gard(self.get_points_ref()) {
                self.shift(|| (0, -1));
                break;
            }
        }
    }

    fn range(&mut self) -> Rect {

        let mut min_x = i32::max_value();
        let mut max_x = i32::min_value();
        let mut min_y = i32::max_value();
        let mut max_y = i32::min_value();

        let points = self.get_points_ref();
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
}

struct BlockHandler {
    points: Points,
}

impl PointTransformer for BlockHandler {
    fn get_points_ref(&mut self) -> &Points {
        &self.points
    }

    fn replace(&mut self, target_points: &mut Points) {
        self.points.truncate(0);
        self.points.append(target_points);
    }
}

impl BlockHandler {
    fn new(points: Points) -> BlockHandler {
        BlockHandler { points: points }
    }

    fn get_points(&self) -> Points {
        self.points.clone()
    }

    fn handle<GARD>(&mut self, event: &BlockEvent, block_type: &BlockType, grid_checker: GARD)
    where
        GARD: Fn(&Points) -> bool,
    {
        match event {
            &BlockEvent::Rotate => {
                if block_type != &BlockType::O {
                    self.rotate();
                }
            }
            &BlockEvent::Left => self.move_left(grid_checker),
            &BlockEvent::Right => self.move_right(grid_checker),
            &BlockEvent::Down => self.move_down(grid_checker),
            &BlockEvent::Drop => self.drop_down(grid_checker),
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

#[derive(PartialEq, Eq, Hash)]
enum BlockEvent {
    Left,
    Right,
    Down,
    Drop,
    Rotate,
    None,
}

struct Block {
    block_type: BlockType,
    color: Color,
    event_emmiter: EventEmitter<AppEvent>,
    points: Points,
    next: Option<Box<Block>>,
}

impl Block {
    fn new(block_type: BlockType) -> Block {
        let points = block_type.points();
        let color = block_type.color();
        Block {
            block_type: block_type,
            points: points,
            color: color,
            event_emmiter: EventEmitter::new(),
            next: None,
        }
    }

    fn calc_next(&mut self) {
        let block = Block::new(BlockType::random());
        let points = block.points();
        self.next = Some(Box::new(block));
        self.event_emmiter.emit(AppEvent::ReadyNext(points));
    }

    fn align_to_start(&mut self) {
        let points: Points = self.block_type.points();
        let mut block_handler = BlockHandler::new(points);
        let range = block_handler.range();
        let center = range.width() / 2;
        block_handler.shift(|| {
            (
                (COLUMNS / 2) as i32 - center as i32,
                range.height() as i32 * -1,
            )
        });
        self.points = block_handler.get_points();
    }

    fn reset(&mut self) {
        if let Some(ref mut block) = self.next {
            self.block_type = block.block_type.clone();
            self.color = block.color;
        } else {
            panic!("Error: fail to allocate the next block");
        }

        self.align_to_start();
        let points = self.points();
        self.event_emmiter.emit(AppEvent::NewBlock(points));

        self.calc_next();
    }

    fn points(&self) -> Points {
        self.points.clone()
    }

    fn color(&self) -> Color {
        self.color.clone()
    }

    fn update(&mut self, points: &mut Points, grid: &mut Grid) -> bool {
        let added_to_grid = if !grid.is_empty_below(points) || grid.is_reach_to_end(points) {
            grid.fill(points, &self.block_type);
            true
        } else {
            false
        };

        self.points.truncate(0);
        self.points.append(points);

        added_to_grid
    }

    fn add_block_listener(&mut self, listener: Rc<RefCell<Listener<AppEvent>>>) {
        self.event_emmiter.on(listener);
    }

    fn trigger_landing(&mut self) {
        self.event_emmiter.emit(AppEvent::Landing);
        self.reset();
    }
}

struct Grid {
    data: [[u8; COLUMNS as usize]; ROWS as usize],
}

impl Grid {
    fn new() -> Grid {
        Grid { data: [[0; COLUMNS as usize]; ROWS as usize] }
    }

    fn check_index_rage(&self, point: &Point) -> bool {
        point.y() >= 0 && point.y() < ROWS as i32 && point.x() >= 0 && point.x() < COLUMNS as i32
    }

    fn fill(&mut self, points: &Points, block_type: &BlockType) {
        let index = block_type.index();
        let points: Vec<&Point> = points
            .iter()
            .filter(|point| self.check_index_rage(point))
            .collect();

        for point in points {
            self.data[point.y() as usize][point.x() as usize] = index;
        }
    }

    fn is_reach_to_end(&self, points: &Points) -> bool {
        let c: Vec<&Point> = points
            .iter()
            .filter(|point| point.y() == ROWS as i32 - 1)
            .collect();

        c.len() > 0
    }

    fn is_empty_below(&self, points: &Points) -> bool {
        let mut block_handler = BlockHandler::new(points.clone());
        block_handler.move_down(|_| false);
        self.is_empty(block_handler.get_points_ref())
    }

    fn is_empty(&self, points: &Points) -> bool {
        let c: Vec<&Point> = points
            .iter()
            .filter(|point| self.check_index_rage(point))
            .filter(|point| {
                self.data[point.y() as usize][point.x() as usize] > 0
            })
            .collect();

        c.len() == 0
    }

    fn for_each_cell<F>(&self, mut func: F)
    where
        F: FnMut(i32, i32, u8),
    {
        for r in 0..self.data.len() {
            let row = self.data[r];
            for c in 0..row.len() {
                func(c as i32, r as i32, row[c]);
            }
        }
    }

    fn find_full_row(&self) -> Vec<i32> {
        let mut rows: Vec<i32> = Vec::new();
        for r in (0..self.data.len()).rev() {
            let row = self.data[r];
            let mut filled = 0;
            for c in 0..row.len() {
                if row[c] > 0 {
                    filled += 1;
                }
            }
            if filled == row.len() {
                rows.push(r as i32);
            }
        }
        rows
    }

    fn remove_row(&mut self, row_index: usize) {
        for r in (0..row_index).rev() {
            for c in 0..self.data[r].len() {
                self.data[r + 1][c] = self.data[r][c];
            }

        }

        for c in 0..self.data[0].len() {
            self.data[0][c] = 0;
        }
    }

    fn _print(&self) {
        println!("==========================");
        for r in 0..self.data.len() {
            let row = self.data[r];
            println!("{:?}", row);
        }
        println!("==========================");
        println!("");
    }
}

#[derive(PartialEq)]
enum AppEvent {
    Landing,
    ReadyNext(Points),
    NewBlock(Points),
}

#[derive(PartialEq)]
enum KindOfAppState {
    Start,
    Finish,
}

struct AppState {
    state: KindOfAppState,
    grid: Rc<RefCell<Grid>>,
}

impl AppState {
    fn new(grid: Rc<RefCell<Grid>>) -> AppState {
        AppState {
            state: KindOfAppState::Start,
            grid: grid,
        }
    }

    fn is_finish(&self) -> bool {
        self.state == KindOfAppState::Finish
    }
}

enum Panel {
    Window,
    Main,
    Right,
}

impl Panel {
    fn x(&self) -> i32 {
        match *self {
            Panel::Window => 0,
            Panel::Main => BORDER as i32,
            Panel::Right => BORDER as i32 + COLUMNS as i32 + BORDER as i32,
        }
    }

    fn y(&self) -> i32 {
        match *self {
            Panel::Window => 0,
            Panel::Main => BORDER as i32,
            Panel::Right => BORDER as i32,
        }
    }

    fn width(&self) -> u32 {
        match *self {
            Panel::Window => WINDOW_WIDTH,
            Panel::Main => COLUMNS,
            Panel::Right => RIGHT_PANEL,
        }
    }

    fn height(&self) -> u32 {
        match *self {
            Panel::Window => WINDOW_HEIGHT,
            Panel::Main => ROWS,
            Panel::Right => ROWS,
        }
    }

    fn background(
        &self,
        canvas: &mut Canvas<Window>,
        texture: &mut Texture,
        color: Color,
        border: bool,
    ) {
        let src = Rect::new(self.x(), self.y(), self.width(), self.height());
        let dst = if border {
            Some(Rect::new(
                self.x() * SCALE as i32 - 1,
                self.y() * SCALE as i32 - 1,
                self.width() * SCALE + 2,
                self.height() * SCALE + 2,
            ))
        } else {
            Some(Rect::new(
                self.x() * SCALE as i32,
                self.y() * SCALE as i32,
                self.width() * SCALE,
                self.height() * SCALE,
            ))
        };

        canvas
            .with_texture_canvas(texture, |texture_canvas| {
                texture_canvas.clear();
                texture_canvas.set_draw_color(color);
                texture_canvas.fill_rect(src).unwrap();
            })
            .unwrap();

        canvas.copy(&texture, src, dst).unwrap();
    }

    fn block_piece(
        &self,
        canvas: &mut Canvas<Window>,
        texture: &mut Texture,
        x: i32,
        y: i32,
        color: Color,
    ) {
        let src = Some(Rect::new(x, y, 1, 1));
        let dst = Some(Rect::new(
            x * SCALE as i32 + 1,
            y * SCALE as i32 + 1,
            SCALE - 2,
            SCALE - 2,
        ));

        canvas
            .with_texture_canvas(texture, |texture_canvas| {
                texture_canvas.set_draw_color(color);
                texture_canvas.draw_point(Point::new(x, y)).unwrap();
            })
            .unwrap();

        canvas.copy(&texture, src, dst).unwrap();
    }

    fn block(
        &self,
        canvas: &mut Canvas<Window>,
        texture: &mut Texture,
        color: Color,
        points: &Points,
    ) {
        for point in points {
            if point.y() < 0 {
                continue;
            }
            self.block_piece(
                canvas,
                texture,
                self.x() + point.x(),
                self.y() + point.y(),
                color,
            );
        }
    }

    fn grid(&self, canvas: &mut Canvas<Window>, texture: &mut Texture, grid: &Grid) {
        grid.for_each_cell(|x, y, value| if value > 0 {
            self.block_piece(
                canvas,
                texture,
                self.x() + x,
                self.y() + y,
                BlockType::new(value).color(),
            );
        });
    }
}

struct App<'a> {
    canvas: Canvas<Window>,
    texture: Texture<'a>,
    events: EventPump,
    block: Block,
    gravity: Gravity,
    grid: Rc<RefCell<Grid>>,
    app_state: Rc<RefCell<AppState>>,
}

impl<'a> App<'a> {
    fn new(
        canvas: WindowCanvas,
        events: EventPump,
        texture_creator: &'a TextureCreator<WindowContext>,
    ) -> App {

        let texture = texture_creator
            .create_texture_target(None, WINDOW_WIDTH, WINDOW_HEIGHT)
            .unwrap();

        let mut block = Block::new(BlockType::random());
        block.calc_next();
        block.align_to_start();

        let grid = Grid::new();
        let grid = Rc::new(RefCell::new(grid));
        let app_state = Rc::new(RefCell::new(AppState::new(grid.clone())));

        block.add_block_listener(grid.clone());
        block.add_block_listener(app_state.clone());

        App {
            canvas: canvas,
            texture: texture,
            events: events,
            block: block,
            gravity: Gravity::new(DEFAULT_GRAVITY),
            grid: grid.clone(),
            app_state: app_state.clone(),
        }
    }

    fn event(&mut self) -> HashSet<BlockEvent> {
        let mut events: HashSet<BlockEvent> = self.events
            .poll_iter()
            .map(|event| match event {
                Event::KeyDown { keycode: Some(Keycode::Up), .. } => BlockEvent::Rotate,
                Event::KeyDown { keycode: Some(Keycode::Left), .. } => BlockEvent::Left,
                Event::KeyDown { keycode: Some(Keycode::Right), .. } => BlockEvent::Right,
                Event::KeyDown { keycode: Some(Keycode::Down), .. } => BlockEvent::Down,
                Event::KeyDown { keycode: Some(Keycode::Space), .. } => BlockEvent::Drop,
                _ => BlockEvent::None,
            })
            .collect();

        match EVENT_Q.lock() {
            Ok(mut v) => {
                while let Some(e) = v.pop() {
                    events.insert(e);
                }
            }
            Err(_) => (),
        }
        events
    }

    fn block_gravity(&mut self) {
        let mut block_handler = BlockHandler::new(self.block.points().clone());

        let computed = self.gravity.compute(
            &self.block.points,
            block_handler.range(),
        );

        if let Some(mut points) = computed {
            if self.block.update(&mut points, &mut self.grid.borrow_mut()) {
                self.block.trigger_landing();
            }
        }
    }

    fn block_event(&mut self, event: &BlockEvent) {
        let mut block_handler = BlockHandler::new(self.block.points().clone());
        block_handler.handle(&event, &self.block.block_type, |points| {
            !self.grid.borrow().is_empty(points)
        });

        if self.block.update(
            &mut block_handler.get_points(),
            &mut self.grid.borrow_mut(),
        )
        {
            self.block.trigger_landing();
        }
    }

    fn present(&mut self) {
        self.canvas.present();
    }

    fn draw_background(&mut self) {
        let (r, g, b) = COLOR_BLACK;
        self.canvas.set_draw_color(Color::RGB(r, g, b));
        self.canvas.clear();

        // Panel::Window.background(
        //     &mut self.canvas,
        //     &mut self.texture,
        //     Color::RGB(r, g, b),
        //     false,
        // );

        let (r, g, b) = COLOR_BLUE;
        Panel::Main.background(
            &mut self.canvas,
            &mut self.texture,
            Color::RGB(r, g, b),
            true,
        );
        let (r, g, b) = COLOR_BLACK;
        Panel::Main.background(
            &mut self.canvas,
            &mut self.texture,
            Color::RGB(r, g, b),
            false,
        );

        // let (r, g, b) = COLOR_ORANGE;
        // Panel::Right.background(
        //     &mut self.canvas,
        //     &mut self.texture,
        //     Color::RGB(r, g, b),
        //     true,
        // );
        // let (r, g, b) = COLOR_BLACK;
        // Panel::Right.background(
        //     &mut self.canvas,
        //     &mut self.texture,
        //     Color::RGB(r, g, b),
        //     false,
        // );
    }

    fn draw_block(&mut self) {
        Panel::Main.block(
            &mut self.canvas,
            &mut self.texture,
            self.block.color(),
            &self.block.points(),
        );

        if let Some(ref block) = self.block.next {
            let mut block_handler = BlockHandler::new(block.points());
            block_handler.shift(|| (1, 1));

            Panel::Right.block(
                &mut self.canvas,
                &mut self.texture,
                block.color(),
                &block_handler.get_points_ref(),
            );
        }
    }

    fn draw_grid(&mut self) {
        Panel::Main.grid(&mut self.canvas, &mut self.texture, &self.grid.borrow());
    }

    fn run(&mut self) {
        if self.app_state.borrow().is_finish() {
            //
        } else {
            for event in self.event() {
                self.block_event(&event);
            }
            self.block_gravity();

            self.draw_background();
            self.draw_block();
            self.draw_grid();

            self.present();
        }


    }
}

trait Listener<T> {
    fn listen(&mut self, event: &T);
}

struct EventEmitter<T> {
    listeners: Vec<Rc<RefCell<Listener<T>>>>,
}

impl<T> EventEmitter<T> {
    fn new() -> EventEmitter<T> {
        EventEmitter { listeners: Vec::new() }
    }

    fn on(&mut self, listener: Rc<RefCell<Listener<T>>>) {
        self.listeners.push(listener);
    }

    fn emit(&mut self, event: T) {
        let ref listeners = self.listeners;
        for l in listeners {
            l.borrow_mut().listen(&event);
        }
    }
}

impl<'a> Listener<AppEvent> for Grid {
    fn listen(&mut self, event: &AppEvent) {
        if event == &AppEvent::Landing {
            for row in self.find_full_row() {
                self.remove_row(row as usize);
            }
        }
    }
}

impl<'a> Listener<AppEvent> for AppState {
    fn listen(&mut self, event: &AppEvent) {
        if let &AppEvent::NewBlock(ref points) = event {
            if !self.grid.borrow().is_empty_below(points) {
                self.state = KindOfAppState::Finish;
            }
        }
    }
}
