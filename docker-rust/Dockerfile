FROM ubuntu:latest

RUN apt-get update \
    && apt-get install -y curl file sudo build-essential libssl-dev

RUN apt-get install -qq gcc-arm-linux-gnueabihf

RUN curl -O https://www.openssl.org/source/openssl-1.1.0e.tar.gz \
    && tar xzf openssl-1.1.0e.tar.gz \
    && export MACHINE=armv7 \
    && export ARCH=arm \
    && export CC=arm-linux-gnueabihf-gcc \
    && cd openssl-1.1.0e \
    && ./config no-shared\
    && make
    
ENV PATH "/root/.cargo/bin:$PATH"

RUN curl https://sh.rustup.rs > rustup.sh \
    && sh rustup.sh -y \
    && rustup target add armv7-unknown-linux-gnueabihf \
    && mkdir -p ~/.cargo \
    && echo "[target.armv7-unknown-linux-gnueabihf]\nlinker = \"arm-linux-gnueabihf-gcc\"" > ~/.cargo/config

RUN echo "export OPENSSL_LIB_DIR=/openssl-1.1.0e" > /release.sh
RUN echo "export OPENSSL_INCLUDE_DIR=/openssl-1.1.0e/include" >> /release.sh
RUN echo "export OPENSSL_STATIC=1" >> /release.sh
RUN echo "cargo build --release --target=armv7-unknown-linux-gnueabihf" > /release.sh

VOLUME /work
WORKDIR /work

CMD ["/bin/bash", "/release.sh"]