package fs.playground
//
//import kotlinx.coroutines.*
//import kotlinx.coroutines.reactor.ReactorContext
//import org.springframework.util.Assert
//import java.util.concurrent.Executors
//import kotlin.coroutines.AbstractCoroutineContextElement
//import kotlin.coroutines.CoroutineContext
//
//open class FsContext : AbstractCoroutineContextElement(FsContext) {
//    companion object Key : CoroutineContext.Key<FsContext>
//}
//
//object READONLY {
//
//    private val localThread = ThreadLocal<Boolean>()
//
//    fun get(): Boolean {
//        return localThread.get() == true
//    }
//
//    fun set() {
//        println("## set ${Thread.currentThread()}")
//        localThread.set(true)
//    }
//
//    fun remove() {
//        println("## remove ${Thread.currentThread()}")
//        localThread.remove()
//    }
//}
//
//class ReadOnlyContext(val value: Boolean? = null) : ThreadContextElement<Boolean?>, AbstractCoroutineContextElement(ReadOnlyContext) {
//    companion object Key : CoroutineContext.Key<ReadOnlyContext>
//
//    override fun updateThreadContext(context: CoroutineContext): Boolean? {
//        val old = READONLY.get()
//        if (value == true) {
//            READONLY.set()
//        }
//        return old
//    }
//
//    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean?) {
//        if (oldState == true) {
//            READONLY.set()
//        } else {
//            READONLY.remove()
//        }
//    }
//}
//
//object FsDispatcherBak {
//
//    private val UUIDS = ThreadLocal<String>()
//
//    private val threadPool = Executors.newFixedThreadPool(8).asCoroutineDispatcher()
//
//    suspend fun <T> asAsyncWithContext(call: () -> T): T {
//        return withContext(FsContext() + threadPool) {
//            val outerName1 = Thread.currentThread().name
//
//            val ret = withContext(coroutineContext + threadPool) {
//                val name1 = Thread.currentThread().name
//                val uuid: String = coroutineContext[ReactorContext]!!.context!!.get("uuid")
//                UUIDS.set(uuid)
//
//                val ret = try {
//                    call()
//                } finally {
//                    val name2 = Thread.currentThread().name
//                    Assert.isTrue(name1 == name2, "$name1 != $name2")
//                    UUIDS.remove()
//                }
//
//                Assert.isTrue(UUIDS.get() == null, "unclear uuid: ${UUIDS.get()}")
//                ret
//            }
//
//            val outerName2 = Thread.currentThread().name
////            Assert.isTrue(outerName1 == outerName2, "$outerName1 != $outerName2")
//            ret
//        }
//    }
//
//    suspend fun <T> asAsync(call: () -> T): Deferred<T> {
//        val uuid: String = withContext(FsContext()) {
//            coroutineContext[ReactorContext]!!.context!!.get("uuid")
//        }
//
//        return CoroutineScope(FsContext() + threadPool).async {
//            val name1 = Thread.currentThread().name
//            val uuid: String = coroutineContext[ReactorContext]?.let { rc -> rc.context?.get("uuid") } ?: uuid
//            UUIDS.set(uuid)
//
//            val ret = try {
//                call()
//            } finally {
//                val name2 = Thread.currentThread().name
//                Assert.isTrue(name1 == name2, "$name1 != $name2")
//                UUIDS.remove()
//            }
//
//            Assert.isTrue(UUIDS.get() == null, "unclear uuid: ${UUIDS.get()}")
//            ret
//        }
//    }
//
//    suspend fun <T> asReadonlyTransaction(call: suspend () -> T): T {
//        return withContext(ReadOnlyContext(true) + FsContext() + threadPool) {
//            val map: MutableMap<String, Any> = coroutineContext[ReactorContext]!!.context!!.get("map")
//            val readonlyCount = map.getOrDefault("readonly", "0").toString().toInt()
//            map["readonly"] = readonlyCount + 1
//            val ret = try {
//                call()
//            } finally {
//                map["readonly"] = map["readonly"].toString().toInt() - 1
//            }
//            println("readonly done: ${map["readonly"]}")
//            ret
//        }
//    }
//
//    suspend fun <T> asRestoration(call: (String) -> T): T {
//        return withContext(FsContext()) {
//            val name1 = Thread.currentThread().name
//            val uuid: String = coroutineContext[ReactorContext]!!.context!!.get("uuid")
//            UUIDS.set(uuid)
//
//            val map: MutableMap<String, Any> = coroutineContext[ReactorContext]!!.context!!.get("map")
//            val readonlyCount = map.getOrDefault("readonly", "0").toString().toInt()
////            println("readonly - asRestoration $readonlyCount")
//            val oldReadOnly = READONLY.get()
//            if (readonlyCount == 0) {
//                READONLY.remove()
//            }
//
//            val ret = try {
//                call(uuid)
//            } finally {
//                val name2 = Thread.currentThread().name
//                Assert.isTrue(name1 == name2, "$name1 != $name2")
//                UUIDS.remove()
//                if (oldReadOnly) {
//                    READONLY.set()
//                }
//            }
//
//            Assert.isTrue(UUIDS.get() == null, "unclear uuid: ${UUIDS.get()}")
//            ret
//        }
//    }
//
//    fun isCurrentTransactionReadOnly(): Boolean {
//        return READONLY.get()
//    }
//
//    fun getUUID(): String? {
//        return UUIDS.get()
//    }
//}