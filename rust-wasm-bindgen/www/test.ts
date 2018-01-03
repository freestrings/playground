
            /* tslint:disable */
            let memory: WebAssembly.Memory;

                function getStringFromWasm(ptr: number, len: number): string {
                    const mem = new Uint8Array(memory.buffer);
                    const slice = mem.slice(ptr, ptr + len);
                    const ret = new TextDecoder('utf-8').decode(slice);
                    return ret;
                }
            let wasm_exports: WasmExports;


            interface WasmImportsTop {
                env: WasmImports,
            }

            interface WasmImports {
                
            }

            interface WasmExports {
                __fixdfdi(arg0: number): number;
__floatuntidf(arg0: number, arg1: number): number;
__mulodi4(arg0: number, arg1: number, arg2: number): number;
__floatsidf(arg0: number): number;
__divti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__floatunsisf(arg0: number): number;
rust_eh_personality(): void;
__floatdidf(arg0: number): number;
__fixdfti(arg0: number, arg1: number): void;
c(arg0: number): number;
__fixunssfti(arg0: number, arg1: number): void;
__mulsf3(arg0: number, arg1: number): number;
__fixunssfdi(arg0: number): number;
__floatsisf(arg0: number): number;
__umodsi3(arg0: number, arg1: number): number;
__udivmodsi4(arg0: number, arg1: number, arg2: number): number;
__divmoddi4(arg0: number, arg1: number, arg2: number): number;
__powisf2(arg0: number, arg1: number): number;
__muldf3(arg0: number, arg1: number): number;
__multi3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__lshrti3(arg0: number, arg1: number, arg2: number, arg3: number): void;
__udivti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__fixunssfsi(arg0: number): number;
__floatundidf(arg0: number): number;
__fixsfti(arg0: number, arg1: number): void;
__fixsfdi(arg0: number): number;
__umodti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__mulosi4(arg0: number, arg1: number, arg2: number): number;
__divsf3(arg0: number, arg1: number): number;
__divdi3(arg0: number, arg1: number): number;
__muldi3(arg0: number, arg1: number): number;
__fixdfsi(arg0: number): number;
__divmodsi4(arg0: number, arg1: number, arg2: number): number;
__fixunsdfdi(arg0: number): number;
a(arg0: number): number;
e(arg0: number): void;
__divdf3(arg0: number, arg1: number): number;
memmove(arg0: number, arg1: number, arg2: number): number;
__modsi3(arg0: number, arg1: number): number;
__ashlti3(arg0: number, arg1: number, arg2: number, arg3: number): void;
__ashldi3(arg0: number, arg1: number): number;
__udivsi3(arg0: number, arg1: number): number;
memset(arg0: number, arg1: number, arg2: number): number;
__adddf3(arg0: number, arg1: number): number;
memory: WebAssembly.Memory;
memcpy(arg0: number, arg1: number, arg2: number): number;
b(arg0: number, arg1: number): number;
__fixsfsi(arg0: number): number;
__udivmoddi4(arg0: number, arg1: number, arg2: number): number;
__muloti4(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number, arg5: number): void;
__divsi3(arg0: number, arg1: number): number;
__floattidf(arg0: number, arg1: number): number;
__modti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__addsf3(arg0: number, arg1: number): number;
__subdf3(arg0: number, arg1: number): number;
__ashrdi3(arg0: number, arg1: number): number;
memcmp(arg0: number, arg1: number, arg2: number): number;
__udivmodti4(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number, arg5: number): void;
__umoddi3(arg0: number, arg1: number): number;
__fixunsdfsi(arg0: number): number;
__floatuntisf(arg0: number, arg1: number): number;
__lshrdi3(arg0: number, arg1: number): number;
__powidf2(arg0: number, arg1: number): number;
__floattisf(arg0: number, arg1: number): number;
__ashrti3(arg0: number, arg1: number, arg2: number, arg3: number): void;
d(arg0: number): number;
__fixunsdfti(arg0: number, arg1: number): void;
__udivdi3(arg0: number, arg1: number): number;
__subsf3(arg0: number, arg1: number): number;
__floatunsidf(arg0: number): number;
__moddi3(arg0: number, arg1: number): number;
            }

            export interface Imports {
                
            }

            

            export interface Exports {
                module: WebAssembly.Module;
                instance: WebAssembly.Module;
                sum(arg0: number): string;
sum_all(arg0: number, arg1: number): string;
extra: ExtraExports;

            }

            export interface ExtraExports {
__fixdfdi(arg0: number): number;
__floatuntidf(arg0: number, arg1: number): number;
__mulodi4(arg0: number, arg1: number, arg2: number): number;
__floatsidf(arg0: number): number;
__divti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__floatunsisf(arg0: number): number;
rust_eh_personality(): void;
__floatdidf(arg0: number): number;
__fixdfti(arg0: number, arg1: number): void;
__fixunssfti(arg0: number, arg1: number): void;
__mulsf3(arg0: number, arg1: number): number;
__fixunssfdi(arg0: number): number;
__floatsisf(arg0: number): number;
__umodsi3(arg0: number, arg1: number): number;
__udivmodsi4(arg0: number, arg1: number, arg2: number): number;
__divmoddi4(arg0: number, arg1: number, arg2: number): number;
__powisf2(arg0: number, arg1: number): number;
__muldf3(arg0: number, arg1: number): number;
__multi3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__lshrti3(arg0: number, arg1: number, arg2: number, arg3: number): void;
__udivti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__fixunssfsi(arg0: number): number;
__floatundidf(arg0: number): number;
__fixsfti(arg0: number, arg1: number): void;
__fixsfdi(arg0: number): number;
__umodti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__mulosi4(arg0: number, arg1: number, arg2: number): number;
__divsf3(arg0: number, arg1: number): number;
__divdi3(arg0: number, arg1: number): number;
__muldi3(arg0: number, arg1: number): number;
__fixdfsi(arg0: number): number;
__divmodsi4(arg0: number, arg1: number, arg2: number): number;
__fixunsdfdi(arg0: number): number;
__divdf3(arg0: number, arg1: number): number;
memmove(arg0: number, arg1: number, arg2: number): number;
__modsi3(arg0: number, arg1: number): number;
__ashlti3(arg0: number, arg1: number, arg2: number, arg3: number): void;
__ashldi3(arg0: number, arg1: number): number;
__udivsi3(arg0: number, arg1: number): number;
memset(arg0: number, arg1: number, arg2: number): number;
__adddf3(arg0: number, arg1: number): number;
memory: WebAssembly.Memory;
memcpy(arg0: number, arg1: number, arg2: number): number;
__fixsfsi(arg0: number): number;
__udivmoddi4(arg0: number, arg1: number, arg2: number): number;
__muloti4(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number, arg5: number): void;
__divsi3(arg0: number, arg1: number): number;
__floattidf(arg0: number, arg1: number): number;
__modti3(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number): void;
__addsf3(arg0: number, arg1: number): number;
__subdf3(arg0: number, arg1: number): number;
__ashrdi3(arg0: number, arg1: number): number;
memcmp(arg0: number, arg1: number, arg2: number): number;
__udivmodti4(arg0: number, arg1: number, arg2: number, arg3: number, arg4: number, arg5: number): void;
__umoddi3(arg0: number, arg1: number): number;
__fixunsdfsi(arg0: number): number;
__floatuntisf(arg0: number, arg1: number): number;
__lshrdi3(arg0: number, arg1: number): number;
__powidf2(arg0: number, arg1: number): number;
__floattisf(arg0: number, arg1: number): number;
__ashrti3(arg0: number, arg1: number, arg2: number, arg3: number): void;
__fixunsdfti(arg0: number, arg1: number): void;
__udivdi3(arg0: number, arg1: number): number;
__subsf3(arg0: number, arg1: number): number;
__floatunsidf(arg0: number): number;
__moddi3(arg0: number, arg1: number): number;
}


            function xform(obj: WebAssembly.ResultObject): Exports {
                let { module, instance } = obj;
                let exports: WasmExports = instance.exports;
                memory = exports.memory;
wasm_exports = exports;

                return {
                module,
                instance,
        sum: function sum(arg0: number): string {
        const ret = wasm_exports.a(arg0);
                
                    const ptr = wasm_exports.d(ret);
                    const len = wasm_exports.c(ret);
                    const realRet = getStringFromWasm(ptr, len);
                    wasm_exports.e(ret);
                    return realRet;
                
            },
sum_all: function sum_all(arg0: number, arg1: number): string {
        const ret = wasm_exports.b(arg0, arg1);
                
                    const ptr = wasm_exports.d(ret);
                    const len = wasm_exports.c(ret);
                    const realRet = getStringFromWasm(ptr, len);
                    wasm_exports.e(ret);
                    return realRet;
                
            },
extra: {
__fixdfdi:exports.__fixdfdi,
__floatuntidf:exports.__floatuntidf,
__mulodi4:exports.__mulodi4,
__floatsidf:exports.__floatsidf,
__divti3:exports.__divti3,
__floatunsisf:exports.__floatunsisf,
rust_eh_personality:exports.rust_eh_personality,
__floatdidf:exports.__floatdidf,
__fixdfti:exports.__fixdfti,
__fixunssfti:exports.__fixunssfti,
__mulsf3:exports.__mulsf3,
__fixunssfdi:exports.__fixunssfdi,
__floatsisf:exports.__floatsisf,
__umodsi3:exports.__umodsi3,
__udivmodsi4:exports.__udivmodsi4,
__divmoddi4:exports.__divmoddi4,
__powisf2:exports.__powisf2,
__muldf3:exports.__muldf3,
__multi3:exports.__multi3,
__lshrti3:exports.__lshrti3,
__udivti3:exports.__udivti3,
__fixunssfsi:exports.__fixunssfsi,
__floatundidf:exports.__floatundidf,
__fixsfti:exports.__fixsfti,
__fixsfdi:exports.__fixsfdi,
__umodti3:exports.__umodti3,
__mulosi4:exports.__mulosi4,
__divsf3:exports.__divsf3,
__divdi3:exports.__divdi3,
__muldi3:exports.__muldi3,
__fixdfsi:exports.__fixdfsi,
__divmodsi4:exports.__divmodsi4,
__fixunsdfdi:exports.__fixunsdfdi,
__divdf3:exports.__divdf3,
memmove:exports.memmove,
__modsi3:exports.__modsi3,
__ashlti3:exports.__ashlti3,
__ashldi3:exports.__ashldi3,
__udivsi3:exports.__udivsi3,
memset:exports.memset,
__adddf3:exports.__adddf3,
memory:exports.memory,
memcpy:exports.memcpy,
__fixsfsi:exports.__fixsfsi,
__udivmoddi4:exports.__udivmoddi4,
__muloti4:exports.__muloti4,
__divsi3:exports.__divsi3,
__floattidf:exports.__floattidf,
__modti3:exports.__modti3,
__addsf3:exports.__addsf3,
__subdf3:exports.__subdf3,
__ashrdi3:exports.__ashrdi3,
memcmp:exports.memcmp,
__udivmodti4:exports.__udivmodti4,
__umoddi3:exports.__umoddi3,
__fixunsdfsi:exports.__fixunsdfsi,
__floatuntisf:exports.__floatuntisf,
__lshrdi3:exports.__lshrdi3,
__powidf2:exports.__powidf2,
__floattisf:exports.__floattisf,
__ashrti3:exports.__ashrti3,
__fixunsdfti:exports.__fixunsdfti,
__udivdi3:exports.__udivdi3,
__subsf3:exports.__subsf3,
__floatunsidf:exports.__floatunsidf,
__moddi3:exports.__moddi3,
},
};
            }
            export function instantiate(bytes: any, _imports: Imports): Promise<Exports> {
                let wasm_imports: WasmImportsTop = {
                    env: {
                        
                    },
                };
                return WebAssembly.instantiate(bytes, wasm_imports).then(xform);
            }
        