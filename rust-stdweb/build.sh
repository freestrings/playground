set -e

hash cargo-web 2>/dev/null || {
    echo "'cargo-web' command not found! ex) cargo install -f cargo-web"
    exit 1
}

cargo-web build --target-webasm --release

[ -e ./static/rust-stdweb.wasm ] && rm ./static/rust-stdweb.wasm
[ -e ./static/rust-stdweb.js ] && rm ./static/js/rust-stdweb.js

mv ./target/wasm32-unknown-unknown/release/rust-stdweb.wasm ./static/
mv ./target/wasm32-unknown-unknown/release/rust-stdweb.js ./static/js/
