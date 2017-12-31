set -e
DIR="$( pwd )"
echo "Current Directory: '$DIR'"

[ -e www/testa.wasm ] && rm www/testa.wasm

cargo +nightly build --target wasm32-unknown-unknown --release

mv "target/wasm32-unknown-unknown/release/rust_wasm.wasm" www/testa.wasm
