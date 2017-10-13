extern crate bytes;
extern crate futures;
extern crate tokio_core;
extern crate tokio_io;
extern crate tokio_service;

use std::{thread, time, io, str};
use bytes::BytesMut;
use futures::{future, Future, Stream, Sink};
use tokio_core::net::TcpListener;
use tokio_core::reactor::Core;
use tokio_io::AsyncRead;
use tokio_io::codec::{Encoder, Decoder};
use tokio_service::{Service, NewService};

pub struct LineCodec;

impl Decoder for LineCodec {
    type Item = String;
    type Error = io::Error;

    fn decode(&mut self, buf: &mut BytesMut) -> io::Result<Option<String>> {
        if let Some(i) = buf.iter().position(|&b| b == b'\n') {
            // remove the serialized frame from the buffer.
            let line = buf.split_to(i);

            // Also remove the '\n'
            buf.split_to(1);

            // Turn this data into a UTF string and return it in a Frame.
            match str::from_utf8(&line) {
                Ok(s) => {
                    println!("Receive: {}", s.to_string());
                    Ok(Some(s.to_string()))
                },
                Err(_) => Err(io::Error::new(io::ErrorKind::Other, "invalid UTF-8")),
            }
        } else {
            Ok(None)
        }
    }
}

impl Encoder for LineCodec {
    type Item = String;
    type Error = io::Error;

    fn encode(&mut self, msg: String, buf: &mut BytesMut) -> io::Result<()> {
        println!("Send: {}", msg);
        buf.extend(msg.as_bytes());
        buf.extend(b"\n");
        Ok(())
    }
}

fn serve<S>(s: S) -> io::Result<()>
    where S: NewService<Request = String,
                        Response = String,
                        Error = io::Error> + 'static
{
    let mut core = Core::new()?;
    let handle = core.handle();

    let address = "0.0.0.0:12345".parse().unwrap();
    let listener = TcpListener::bind(&address, &handle)?;

    let connections = listener.incoming();
    let server = connections.for_each(move |(socket, _peer_addr)| {
        let (writer, reader) = socket.framed(LineCodec).split();
        let service = s.new_service()?;

        let responses = reader.and_then(move |req| {
            println!("Reader call service");
            service.call(req)
        });

        let server = writer.send_all(responses).then(|_| {
            println!("Client close before");
            Ok(())
        });

        handle.spawn(server);

        Ok(())
    });

    core.run(server)
}

struct EchoService;

impl Service for EchoService {
    type Request = String;
    type Response = String;
    type Error = io::Error;
    type Future = Box<Future<Item = String, Error = io::Error>>;

    fn call(&self, input: String) -> Self::Future {
        println!("Service::call1: {}", input);
        let ten_millis = time::Duration::from_millis(1000);
        thread::sleep(ten_millis);
        println!("Service::call2: {}", input);
        Box::new(future::ok(input))
    }
}

struct EchoRev;

impl Service for EchoRev {
    type Request = String;
    type Response = String;
    type Error = io::Error;
    type Future = Box<Future<Item = Self::Response, Error = Self::Error>>;

    fn call(&self, req: Self::Request) -> Self::Future {
        let rev: String = req.chars()
            .rev()
            .collect();
        Box::new(future::ok(rev))
    }
}

fn main() {
    if let Err(e) = serve(|| Ok(EchoRev)) {
        println!("Server failed with {}", e);
    }
}

#[cfg(test)]
mod tests {

    use super::*;

    #[test]
    fn bytes_test() {
        use bytes::BufMut;

        let mut buf = BytesMut::with_capacity(64);
        buf.put(&b"line0\nline1\n"[..]);
        if let Some(i) = buf.iter().position(|&b| b == b'\n') {
            buf.split_to(i);
            assert_eq!(&buf[..], b"\nline1\n");
            buf.split_to(1);
            assert_eq!(&buf[..], b"line1\n");
        } else {
            assert!(false);
        }
    }

    #[test]
    fn to_socket_test() {
        use std::net::ToSocketAddrs;
        
        let addr = "www.rust-lang.org:443".to_socket_addrs().unwrap().next().unwrap();
        println!("{:?}", addr);
    }
}
