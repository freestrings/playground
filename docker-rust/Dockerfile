FROM ubuntu:latest

RUN apt-get update \
    && apt-get install -y curl file sudo build-essential

RUN apt-get install -qq gcc-arm-linux-gnueabihf

ENV PATH "/root/.cargo/bin:$PATH"

RUN curl https://sh.rustup.rs > rustup.sh \
    && sh rustup.sh -y \
    && rustup target add armv7-unknown-linux-gnueabihf \
    && mkdir -p ~/.cargo \
    && echo "[target.armv7-unknown-linux-gnueabihf]\nlinker = \"arm-linux-gnueabihf-gcc\"" > ~/.cargo/config

RUN echo "cargo build --release --target=armv7-unknown-linux-gnueabihf" > /release.sh

VOLUME /work
WORKDIR /work

CMD ["/bin/bash", "/release.sh"]