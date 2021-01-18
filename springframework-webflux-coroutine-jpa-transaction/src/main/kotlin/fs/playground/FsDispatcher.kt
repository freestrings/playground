package fs.playground

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import reactor.core.scheduler.Schedulers
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun debugPrint(flag: String, uuid: String? = null, slave: String? = null) {
    println(
        "${(uuid ?: "").padEnd(10)}${flag.padEnd(15)}${(slave ?: "").padEnd(5)}${
            Thread.currentThread().toString().padEnd(50)
        }"
    )
}

class AsyncFsContext(
    var uuid: String,
    private var slave: AtomicInteger = AtomicInteger(0),
    private val data: MutableMap<String, Any?> = mutableMapOf()
) :
    ThreadContextElement<Boolean>,
    AbstractCoroutineContextElement(AsyncFsContext) {

    companion object Key : CoroutineContext.Key<AsyncFsContext>

    override fun updateThreadContext(context: CoroutineContext): Boolean {
        debugPrint("Update", uuid, slave.toString())
        CTX.setUuid(uuid)

        val isSlave = CTX.isSlave()
        if (isSlave()) {
            debugPrint("SlaveSet", uuid, this.slave.toString())
            CTX.setSlave()
        } else {
            debugPrint("SlaveUnSet", uuid, this.slave.toString())
            CTX.clearSlave()
        }
        return isSlave
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean) {
        debugPrint("Restore-$oldState", uuid, slave.toString())

        if (oldState) {
            CTX.setSlave()
        } else {
            CTX.clearSlave()
        }
    }

    fun incSlave() {
        slave.incrementAndGet()
    }

    fun decSlave() {
        slave.decrementAndGet()
    }

    fun isSlave() = slave.get() != 0

    fun putData(key: String, value: Any) {
        data[key] = value
    }

    fun removeData(key: String) {
        data.remove(key)
    }

    fun getData(key: String): Any? {
        return data[key]
    }

    fun getAllData(): Map<String, Any?> {
        return data
    }

    object CTX {

        private val slave = ThreadLocal<Boolean?>()
        private val uuid = ThreadLocal<String?>()

        fun setSlave() {
            slave.set(true)
        }

        fun isSlave() = slave.get() == true

        fun clearSlave() {
            slave.remove()
        }

        fun setUuid(value: String) {
            uuid.set(value)
        }

        fun getUuid() = uuid.get()

    }
}

object FsDispatcher {

    private val threadPool = Schedulers.boundedElastic().asCoroutineDispatcher()

    suspend fun <T> asAsync(call: suspend () -> T): Deferred<T> {
        val fsContext = getAsyncFsContext(coroutineContext)
        val isSlave = fsContext.isSlave()
        debugPrint("Before", fsContext.uuid)
        return CoroutineScope(fsContext + threadPool).async {
            debugPrint("Scope", fsContext.uuid)
            if (isSlave) {
                AsyncFsContext.CTX.setSlave()
            } else {
                AsyncFsContext.CTX.clearSlave()
            }
            call()
        }
    }

    suspend fun putContext(key: String, value: Any) {
        val fsContext = getAsyncFsContext(coroutineContext)
        fsContext.putData(key, value)
    }

    suspend fun getContext(key: String): Any? {
        val fsContext = getAsyncFsContext(coroutineContext)
        return fsContext.getData(key)
    }

    suspend fun getAllContext(): Map<String, Any?> {
        val fsContext = getAsyncFsContext(coroutineContext)
        return fsContext.getAllData()
    }

    suspend fun <T> asContext(value: String, call: suspend (String) -> T): T {
        val fsContext = getAsyncFsContext(coroutineContext)
        val uuid = fsContext.uuid
        return withContext(fsContext + threadPool) {
            if (value != uuid) {
                throw Exception("#$value $uuid")
            }
            call(value)
        }
    }

    suspend fun <T> withSlave(value: String, call: suspend (String) -> T): T {
        val fsContext: AsyncFsContext = getAsyncFsContext(coroutineContext)
        fsContext.incSlave()
        return try {
            withContext(fsContext) {
                call(value)
            }
        } finally {
            fsContext.decSlave()
            if (!fsContext.isSlave()) {
                AsyncFsContext.CTX.clearSlave()
            }
        }
    }

    fun getAsyncFsContext(context: CoroutineContext): AsyncFsContext {
        return context[AsyncFsContext] ?: context[ReactorContext]!!.context!![AsyncFsContext]
    }

}