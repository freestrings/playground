extern crate emscripten_sys as asm;
extern crate sdl2;
extern crate rand;

use sdl2::rect::{Point, Rect};
use sdl2::video::{Window, WindowContext};
use sdl2::render::{Canvas, Texture, TextureCreator, WindowCanvas};
use sdl2::pixels::Color;

use std::mem;
use std::os::raw::c_void;

fn main() {
    let sdl_context = sdl2::init().unwrap();
    let video_subsystem = sdl_context.video().unwrap();

    let window = video_subsystem.window("Test", 640, 480)
        .build()
        .unwrap();

    let canvas: WindowCanvas =
        window.into_canvas().accelerated().target_texture().build().unwrap();

    init(canvas);

}

struct Args<'a> {
    canvas: Canvas<Window>,
    texture: Texture<'a>,
    angle: f64,
}

fn init(canvas: Canvas<Window>) {
    let texture_creator: TextureCreator<WindowContext> = canvas.texture_creator();
    let texture: Texture =
        texture_creator.create_texture_target(texture_creator.default_pixel_format(), 160, 160)
            .unwrap();

    let mut args = Box::new(Args {
        canvas: canvas,
        texture: texture,
        angle: 0.0,
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
        let canvas = &mut args.canvas;
        let texture = &mut args.texture;

        args.angle = (args.angle + 10.5) % 360.;

        canvas.with_texture_canvas(texture, |texture_canvas| {
                texture_canvas.clear();
                texture_canvas.set_draw_color(Color::RGBA(255, 0, 0, 100));
                texture_canvas.fill_rect(Rect::new(0, 0, 160, 160)).unwrap();
            })
            .unwrap();

        canvas.set_draw_color(Color::RGBA(0, 0, 0, 255));

        let dst = Some(Rect::new(240, 160, 160, 160));

        canvas.clear();
        canvas.copy_ex(&texture,
                     None,
                     dst,
                     args.angle,
                     Some(Point::new(80, 80)),
                     false,
                     false)
            .unwrap();
        canvas.present();

    }
}
