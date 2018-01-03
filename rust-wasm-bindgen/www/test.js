/* tslint:disable */
var memory;
function getStringFromWasm(ptr, len) {
    var mem = new Uint8Array(memory.buffer);
    var slice = mem.slice(ptr, ptr + len);
    var ret = new TextDecoder('utf-8').decode(slice);
    return ret;
}
var wasm_exports;
function xform(obj) {
    var module = obj.module, instance = obj.instance;
    var exports = instance.exports;
    memory = exports.memory;
    wasm_exports = exports;
    return {
        module: module,
        instance: instance,
        sum: function sum(arg0) {
            var ret = wasm_exports.a(arg0);
            var ptr = wasm_exports.d(ret);
            var len = wasm_exports.c(ret);
            var realRet = getStringFromWasm(ptr, len);
            wasm_exports.e(ret);
            return realRet;
        },
        sum_all: function sum_all(arg0, arg1) {
            var ret = wasm_exports.b(arg0, arg1);
            var ptr = wasm_exports.d(ret);
            var len = wasm_exports.c(ret);
            var realRet = getStringFromWasm(ptr, len);
            wasm_exports.e(ret);
            return realRet;
        },
        extra: {
            __fixdfdi: exports.__fixdfdi,
            __floatuntidf: exports.__floatuntidf,
            __mulodi4: exports.__mulodi4,
            __floatsidf: exports.__floatsidf,
            __divti3: exports.__divti3,
            __floatunsisf: exports.__floatunsisf,
            rust_eh_personality: exports.rust_eh_personality,
            __floatdidf: exports.__floatdidf,
            __fixdfti: exports.__fixdfti,
            __fixunssfti: exports.__fixunssfti,
            __mulsf3: exports.__mulsf3,
            __fixunssfdi: exports.__fixunssfdi,
            __floatsisf: exports.__floatsisf,
            __umodsi3: exports.__umodsi3,
            __udivmodsi4: exports.__udivmodsi4,
            __divmoddi4: exports.__divmoddi4,
            __powisf2: exports.__powisf2,
            __muldf3: exports.__muldf3,
            __multi3: exports.__multi3,
            __lshrti3: exports.__lshrti3,
            __udivti3: exports.__udivti3,
            __fixunssfsi: exports.__fixunssfsi,
            __floatundidf: exports.__floatundidf,
            __fixsfti: exports.__fixsfti,
            __fixsfdi: exports.__fixsfdi,
            __umodti3: exports.__umodti3,
            __mulosi4: exports.__mulosi4,
            __divsf3: exports.__divsf3,
            __divdi3: exports.__divdi3,
            __muldi3: exports.__muldi3,
            __fixdfsi: exports.__fixdfsi,
            __divmodsi4: exports.__divmodsi4,
            __fixunsdfdi: exports.__fixunsdfdi,
            __divdf3: exports.__divdf3,
            memmove: exports.memmove,
            __modsi3: exports.__modsi3,
            __ashlti3: exports.__ashlti3,
            __ashldi3: exports.__ashldi3,
            __udivsi3: exports.__udivsi3,
            memset: exports.memset,
            __adddf3: exports.__adddf3,
            memory: exports.memory,
            memcpy: exports.memcpy,
            __fixsfsi: exports.__fixsfsi,
            __udivmoddi4: exports.__udivmoddi4,
            __muloti4: exports.__muloti4,
            __divsi3: exports.__divsi3,
            __floattidf: exports.__floattidf,
            __modti3: exports.__modti3,
            __addsf3: exports.__addsf3,
            __subdf3: exports.__subdf3,
            __ashrdi3: exports.__ashrdi3,
            memcmp: exports.memcmp,
            __udivmodti4: exports.__udivmodti4,
            __umoddi3: exports.__umoddi3,
            __fixunsdfsi: exports.__fixunsdfsi,
            __floatuntisf: exports.__floatuntisf,
            __lshrdi3: exports.__lshrdi3,
            __powidf2: exports.__powidf2,
            __floattisf: exports.__floattisf,
            __ashrti3: exports.__ashrti3,
            __fixunsdfti: exports.__fixunsdfti,
            __udivdi3: exports.__udivdi3,
            __subsf3: exports.__subsf3,
            __floatunsidf: exports.__floatunsidf,
            __moddi3: exports.__moddi3
        }
    };
}
export function instantiate(bytes, _imports) {
    var wasm_imports = {
        env: {}
    };
    return WebAssembly.instantiate(bytes, wasm_imports).then(xform);
}
