async function main() {
    const Module = {};

    return fetchAndInstantiate('testa.wasm', {
        env: {
            hello_as_ptr(ptr, len) {
                console.log('hello_as_ptr - getStr', getStr(Module, ptr, len), len);
                // console.log('hello_as_ptr - copyCStr', copyCStr(Module, ptr), len); // <- 요건 dealloc_str에서 에러
                console.log('hello_as_ptr - copyCStr', copyCStr(Module, ptr / 8), len); // <- 요건 한글 있으면 깨짐
            },

            hello_into_raw(ptr, len) {
                console.log('hello_into_raw - getStr', getStr(Module, ptr, len), len);
                console.log('hello_into_raw - copyCStr', copyCStr(Module, ptr), len);
            }
        }
    }).then(module => {
        Module.alloc = module.exports.alloc;
        Module.dealloc = module.exports.dealloc;
        Module.dealloc_str = module.exports.dealloc_str;
        Module.memory = module.exports.memory;
        return module.exports;
    }).then(module => {
        const mul = module.multiply(4, 5);
        console.log('multiply', mul);

        const an = module.append_num(6);
        console.log('append_num', copyCStr(Module, an));

        const as = module.append_str(newString(Module, 'f한rees'));
        console.log('append_str', copyCStr(Module, as));

        module.callback_str_as_ptr(7, newString(Module, 'f한reest'));
        module.callback_str_into_raw(8, newString(Module, 'f한reest'));
    });
}

main().catch(err => console.error(err));

// -----------------------------------------
//
// https://www.hellorust.com/demos/bundle.js
//

function fetchAndInstantiate(url, importObject) {
    return fetch(url).then(response =>
        response.arrayBuffer()
    ).then(bytes =>
        WebAssembly.instantiate(bytes, importObject)
    ).then(results =>
        results.instance
    );
}

function copyCStr(module, ptr) {
    let orig_ptr = ptr;
    const collectCString = function*() {
        let memory = new Uint8Array(module.memory.buffer);
        while (memory[ptr] !== 0) {
            if (memory[ptr] === undefined) { throw new Error("Tried to read undef mem") }
            yield memory[ptr]
            ptr += 1;
        }
    }

    const buffer_as_u8 = new Uint8Array(collectCString())
    const utf8Decoder = new TextDecoder("UTF-8");
    const buffer_as_utf8 = utf8Decoder.decode(buffer_as_u8);
    module.dealloc_str(orig_ptr);
    return buffer_as_utf8
}

function getStr(module, ptr, len) {
    const getData = function*(ptr, len) {
        let memory = new Uint8Array(module.memory.buffer);
        for (let index = 0; index < len; index++) {
            if (memory[ptr] === undefined) { throw new Error(`Tried to read undef mem at ${ptr}`) }
            yield memory[ptr + index]
        }
    }

    const buffer_as_u8 = new Uint8Array(getData(ptr, len));
    const utf8Decoder = new TextDecoder("UTF-8");
    const buffer_as_utf8 = utf8Decoder.decode(buffer_as_u8);
    return buffer_as_utf8;
}

function newString(module, str) {
    const utf8Encoder = new TextEncoder("UTF-8");
    let string_buffer = utf8Encoder.encode(str)
    let len = string_buffer.length
    let ptr = module.alloc(len + 1)

    let memory = new Uint8Array(module.memory.buffer);
    for (i = 0; i < len; i++) {
        memory[ptr + i] = string_buffer[i]
    }

    memory[ptr + len] = 0;

    return ptr
}
// -----------------------------------------