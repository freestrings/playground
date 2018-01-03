set -e

cargo build --release --target wasm32-unknown-unknown

hash wasm-bindgen 2>/dev/null || {
    echo "Install wasm-bindgen" \
    cargo install --git https://github.com/alexcrichton/wasm-bindgen
}

wasm-bindgen target/wasm32-unknown-unknown/release/wasm_bindgen_test.wasm \
  --output-ts www/test.ts \
  --output-wasm www/test.wasm

if [ ! -f ./node_modules/typescript/bin/tsc ]; then
    echo "Install typescript"
    npm install typescript @types/webassembly-js-api @types/text-encoding
fi

./node_modules/typescript/bin/tsc www/test.ts --lib es6 -m es2015