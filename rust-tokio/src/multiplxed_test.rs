//#![deny(warnings, missing_docs)]

extern crate tokio_proto;

use bytes::BytesMut;

use futures::Future;

use std::io;
use std::net::SocketAddr;

use tokio_io::{AsyncRead, AsyncWrite};
use tokio_io::codec::{Encoder, Decoder, Framed};
use tokio_core::net::TcpStream;
use self::tokio_proto::{TcpClient, TcpServer};
use self::tokio_proto::multiplex::{RequestId, ClientService, ClientProto, ServerProto};


struct Validate<T> {
    inner: T,
}

struct LineCodec;

impl Encoder for LineCodec {
    type Item = (RequestId, String);
    type Error = io::Error;

    fn encode(&mut self, item: Self::Item, dst: &mut BytesMut) -> Result<(), Self::Error> {
        unimplemented!()
    }
}

impl Decoder for LineCodec {
    type Item = (RequestId, String);
    type Error = io::Error;

    fn decode(&mut self, src: &mut BytesMut) -> Result<Option<Self::Item>, Self::Error> {
        unimplemented!()
    }
}

struct LineProto;

impl<T: AsyncRead + AsyncWrite + 'static> ClientProto<T> for LineProto {
    type Request = String;
    type Response = String;
    type Transport = Framed<T, LineCodec>;
    type BindTransport = Result<Self::Transport, io::Error>;

    fn bind_transport(&self, io: T) -> Self::BindTransport {
        unimplemented!()
    }
}

impl<T: AsyncRead + AsyncWrite + 'static> ServerProto<T> for LineProto {
    type Request = String;
    type Response = String;
    type Transport = Framed<T, LineCodec>;
    type BindTransport = Result<Self::Transport, io::Error>;

    fn bind_transport(&self, io: T) -> Self::BindTransport {
        unimplemented!()
    }
}

struct Client {
    inner: Validate<ClientService<TcpStream, LineProto>>,
}

impl Client {
    pub fn connect(addr: &SocketAddr, handle: &Handle) -> Box<Future<Item = Client, Error = io::Error>> {
        let client = TcpClient::new(LineProto)
            .connect(addr, handle)
            .map(|client_service| {
                let validate = Validate { inner: client_service };
                Client { inner: validate }
            });

        Box::new(client)
    }
}