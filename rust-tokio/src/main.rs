extern crate futures;
extern crate tokio_core;
extern crate tokio_io;

use futures::stream::Stream;
use tokio_core::reactor::Core;
use tokio_core::net::TcpListener;
use std::{thread, time};

fn main() {
    let mut core = Core::new().unwrap();
    let address = "0.0.0.0:12345".parse().unwrap();
    let listener = TcpListener::bind(&address, &core.handle()).unwrap();

    let connections = listener.incoming();
    let welcomes = connections.and_then(|(socket, _peer_addr)| {
        tokio_io::io::write_all(socket, b"Hello, world!\n")
    });
    let server = welcomes.for_each(|(_socket, _welcome)| {
        let ten_millis = time::Duration::from_millis(3000);
        thread::sleep(ten_millis);
        println!("in server");
        Ok(())
    });

    core.run(server).unwrap();
}