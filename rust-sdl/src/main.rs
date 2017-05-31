extern crate sdl2;
use std::path::Path;

use sdl2::rect::Rect;
use sdl2::rect::Point;

use std::os::raw::{c_void, c_int};
use std::ptr::null_mut;

use std::cell::RefCell;

#[allow(non_camel_case_types)]
type em_callback_func = unsafe extern fn();
extern {
    fn emscripten_set_main_loop(func: em_callback_func,
                                fps: c_int,
                                simulate_infinite_loop: c_int);
}

thread_local!(static MAIN_LOOP_CALLBACK: RefCell<*mut c_void> = RefCell::new(null_mut()));

pub fn set_main_loop_callback<F>(callback: F) where F: FnMut() {
    MAIN_LOOP_CALLBACK.with(|log| {
        *log.borrow_mut() = &callback as *const _ as *mut c_void;
    });

    unsafe { emscripten_set_main_loop(wrapper::<F>, 0, 1); }
}

unsafe extern "C" fn wrapper<F>() where F : FnMut() {
    MAIN_LOOP_CALLBACK.with(|z| {
        let closure = *z.borrow_mut() as *mut F;
        (*closure)();
    });
}

fn main() {
    let sdl_context = sdl2::init().unwrap();
    let video_subsystem = sdl_context.video().unwrap();

    let window = video_subsystem.window("SDL2", 640, 480)
        .position_centered().build().unwrap();

    let mut canvas = window.into_canvas()
        .accelerated().build().unwrap();
    let texture_creator = canvas.texture_creator();

    canvas.set_draw_color(sdl2::pixels::Color::RGBA(0,0,0,255));

    let mut timer = sdl_context.timer().unwrap();

    let temp_surface = sdl2::surface::Surface::load_bmp(Path::new("animate.bmp")).unwrap();
    let texture = texture_creator.create_texture_from_surface(&temp_surface).unwrap();
    
    let center = Point::new(320,240);
    let mut source_rect = Rect::new(0, 0, 128, 82);
    let mut dest_rect = Rect::new(0,0, 128, 82);
    dest_rect.center_on(center);

    set_main_loop_callback(|| {
        let ticks = timer.ticks();

        source_rect.set_x((128 * ((ticks / 100) % 6) ) as i32);
        canvas.clear();
        canvas.copy_ex(&texture, Some(source_rect), Some(dest_rect), 10.0, None, true, false).unwrap();
        canvas.present();
    });
}