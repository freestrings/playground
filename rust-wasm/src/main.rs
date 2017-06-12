extern crate emscripten_sys as asm;

extern crate rand;

extern crate graphics;
extern crate opengl_graphics;
extern crate piston;
extern crate sdl2_window;

use opengl_graphics::*;
use piston::window::*;
use sdl2_window::Sdl2Window;

use std::mem;
use std::os::raw::c_void;

use rand::Rng;

fn main() {

    let opengl = OpenGL::V2_1;

    let mut window: Sdl2Window = WindowSettings::new("Hello!", (640 as u32, 480 as u32))
        .opengl(opengl)
        .srgb(false) // important!
        .exit_on_esc(true)
        .build()
        .expect("fail to build window");

    let gl_graphics = GlGraphics::new(opengl);

    run(window, gl_graphics);
}

struct Args {
    gl_graphics: GlGraphics,
}

fn run(window: Sdl2Window, gl_graphics: GlGraphics) {
    let mut args = Box::new(Args { gl_graphics: gl_graphics });
    let args_ptr = &mut *args as *mut Args as *mut c_void;

    unsafe {
        asm::emscripten_set_main_loop_arg(Some(main_loop_callback), args_ptr, 0, 1);
    }

    mem::forget(args);
}

extern "C" fn main_loop_callback(arg: *mut c_void) {
    unsafe {
        let mut args: &mut Args = mem::transmute(arg);
        let gl_graphics = &mut args.gl_graphics;
        let mut rng = rand::thread_rng();
        println!("loop :{}!", rng.gen::<u32>());
    }
}
