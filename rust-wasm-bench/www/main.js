const words = [
    'wmnqiniqjsvai',
    'wvygpbgqiywjj',
    'prlzqrgeuiaxcwxylch',
    'y',
    'i',
    'z',
    'zddztqaehuhwxw',
    'vbopjluwvzulefkgfzbagxrxeuapa',
    'uhcrprarkgbkt',
    'hplldkwrklphwyqgbdcnyghlzaylljxtmpo',
    'qffhgzbdynpbzofasgnmxooyo',
    'otv',
    'naeyzexd',
    'jdyszuzrjqavxqxsbmwiwgykaytopflqvgpblqkor',
    'hgtejjgfuuqn',
    'hrnvn',
    'zplbvhtxgdz',
    'hfzkmccqodact',
    'cbhaxmpbq',
    'ghbgklykouumdprwwilbbvkzqpqirif',
    ',ffzbmhtscplmrxswxvgdsaulepobxxuvsrybzgsdx',
    'rryiwliqrnts',
    'ygxfjwhz',
    'b',
    'wtetobvtuyd',
    'xicddxufbaaoopv',
    'bgccyvvsb',
    'tgyejzwamqbnrqnqxmwibzfewplh',
    'jahnnu',
    'orimkxzlzbqkuvxnhkvzvpjoaqzjsi',
    'egeutplxlmmrxrwduid',
    'sczrunwhvlne',
    'dxbfkwjevvrrbulmedpkocwgeia',
    'ilgchejffowtyxzigtrdhdkos',
    'lhwiyiaobunseolgahqswntn',
    'fazlxdmhhm',
    'mjpvaimkguvewbuf',
    'yqaqjiarsazxlukvzzufegw',
    'tueddrzuwrqglq',
    'ylpqvxpizwaykjfxzzcdjsjioxnywnywjijarkgvzvnxaidupbzzfjwvdghtiqcswgmfkxfcjlnqrxqseaiqfxt',
    'k',
    'bxwfbeanohu',
    'eeitnndinbettnlqedcpkqbzz',
    'xuvdwpkmtsleqoouokjgqmmzihubs',
    'ckfcblhv',
    'hfngdlxbnjffkslbuyunervkwrdpxgrde',
    'csrmrnvzrzwdxnvpog',
    'dvvqwmmvzljdywateafjmztebotavznkbvpgoohckcfumhotmfcfjiijlrkrijhkdpqcpvpaoctyyldeenlcooyeikzyrf',
    'fruqldidjsxqpgoacbcngpawwljhcjyhrzuuyp',
    'y'
];

async function main() {

    // window.Module = {};

    return fetchAndInstantiate('testa.wasm', {}).then(module => module.exports).then(module => {

        const iter = 100000;
        const matchedCount = 0;

        console.log('JS - testRegInJavascript', runIter(iter, () => 
            words.map(word => testRegInJavascript).filter(r => r === true).length === matchedCount)
        );
        console.log('JS - testPrecompiledRegInJavascript', runIter(iter, () => 
            words.map(word => testPrecompiledRegInJavascript).filter(r => r === true).length === matchedCount)
        );

        const bufferedWords = words.map(word => newString(module, word));
        console.log('Rust - testPrecompiledRegInRust', runIter(iter, () => 
            bufferedWords.map(word => testPrecompiledRegInRust(word, module)).filter(r => r === true).length === matchedCount)
        );
        console.log('Rust - testRustInlineIsMatch', runIter(1, () => 
            module.inline_is_match(iter, newString(module, words.join(','))) === matchedCount)
        );

        // // 너무 느림
        // // const regString = newString(module, '^bc(d|e)*$');
        // // console.log('testRegInRust', runIter(iter, () => 
        // //     bufferedWords.map(word => testRegInRust(regString, word, module)).filter(r => r === true).length === matchedCount)
        // // );

        const sumNumber = 1000;
        const sumResult = 499500;
        console.log('JS - sumInJavascript', runIter(iter, () => 
            sumInJavascript(sumNumber) === sumResult)
        );
        console.log('Rust - sumInRust', runIter(iter, () => 
            module.sum_in_rust(sumNumber) === sumResult)
        );
        console.log('Rust - InlineSumInRust', runIter(1, () => 
            module.inline_sum_in_rust(iter, sumNumber) === sumResult)
        );
    });
}

main().catch(err => console.error(err));

function runIter(iter, fn) {
    var d = Date.now();
    Array(iter).fill().forEach(() => {
        if(fn() !== true) throw new Error(fn.toString()) 
    });
    return Date.now() - d;
}

function run(words, matchedCount, fn, module) {
    return words.map(word => fn(word, module)).filter(r => r === true).length === matchedCount;
}

function testPrecompiledRegInRust(string, module) {
    return module.pre_compiled_is_match(string) == 1;
}

function testRegInRust(regString, string, module) {
    return module.is_match(regString, string) === 1;
}

const reg = /^bc(d|e)*$/;
function testPrecompiledRegInJavascript(string) {
    return !!string.match(reg);
}

function testRegInJavascript(string) {
    return !!string.match(/^bc(d|e)*$/);
}

function sumInJavascript(num) {
    var s = 0;
    for(var i = 0 ; i < num ; i++) {
        s += i;
    }
    return s;
}

function fetchAndInstantiate(url, importObject) {
    return fetch(url).then(response =>
        response.arrayBuffer()
    ).then(bytes =>
        WebAssembly.instantiate(bytes, importObject)
    ).then(results =>
        results.instance
    );
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