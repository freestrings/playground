package fs.playground

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

fun debugPrint(flag: String, uuid: String? = null, readOnly: String? = null) {
    val uuidValue = AsyncFsContext.CTX.getUuid() ?: "#$uuid"
    val readValule = if (AsyncFsContext.CTX.isReadOnly()) "true" else "#${readOnly ?: "false"}"
//    println("${flag.padEnd(15)}${Thread.currentThread().toString().padEnd(50)}${uuidValue.padEnd(50)}$readValule")
}

class FsDefaultContext : AbstractCoroutineContextElement(FsDefaultContext) {
    companion object Key : CoroutineContext.Key<FsDefaultContext>
}

class AsyncFsContext(
    val uuid: String? = null,
    private var readonly: AtomicInteger = AtomicInteger(0),
    private val data: MutableMap<String, Any> = mutableMapOf()
) :
    ThreadContextElement<Boolean>,
    AbstractCoroutineContextElement(AsyncFsContext) {

    companion object Key : CoroutineContext.Key<AsyncFsContext>

    override fun updateThreadContext(context: CoroutineContext): Boolean {
        debugPrint("Update", uuid, readonly.toString())
        if (uuid != null) CTX.setUuid(uuid)

        val readonly = CTX.isReadOnly()
        if (isReadOnly()) {
            debugPrint("Update-S", uuid, this.readonly.toString())
            CTX.setReadOnly()
        }
        return readonly
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean) {
        debugPrint("Restore", uuid, readonly.toString())

        if (oldState) {
            CTX.setReadOnly()
        } else {
            debugPrint("Restore-C", uuid, readonly.toString())
            CTX.clearReadOnly()
        }

    }

    fun incReadOnly() {
        readonly.incrementAndGet()
        debugPrint("INC", uuid, readonly.toString())
    }

    fun decReadOnly() {
        readonly.decrementAndGet()
        debugPrint("DEC", uuid, readonly.toString())
    }

    fun isReadOnly() = readonly.get() != 0

    fun putData(key: String, value: Any) {
        data[key] = value
    }

    fun getAllData(): Map<String, Any> {
        return data
    }

    object CTX {

        private val readOnly = ThreadLocal<Boolean?>()
        private val uuid = ThreadLocal<String?>()

        fun setReadOnly() {
            debugPrint("Set")
            readOnly.set(true)
        }

        fun isReadOnly() = readOnly.get() == true

        fun clearReadOnly() {
            debugPrint("Clear")
            readOnly.remove()
        }

        fun setUuid(value: String) {
            uuid.set(value)
        }

        fun getUuid() = uuid.get()

    }
}

object FsDispatcher {

    private val threadPool = Schedulers.boundedElastic().asCoroutineDispatcher()

    suspend fun <T> asAsync(call: () -> T): Deferred<T> {
        return coroutineScope {
            val fsContext: AsyncFsContext = findAsyncFsContext(coroutineContext)
            val isReadOnly = fsContext.isReadOnly()
            CoroutineScope(fsContext + threadPool).async {
                if (isReadOnly) {
                    debugPrint("NewAsync")
                    AsyncFsContext.CTX.setReadOnly()
                } else {
                    AsyncFsContext.CTX.clearReadOnly()
                }
                call()
            }
        }
    }

    suspend fun deferredContext(call: suspend () -> Pair<String, String>) {
        return coroutineScope {
            val fsContext: AsyncFsContext = findAsyncFsContext(coroutineContext)
            val pair = call()
            println("return ${pair.second} - ${Thread.currentThread()}")
            fsContext.putData(pair.first, pair.second)
        }
    }

    /**
     * asNewReadOnly { asNewAsync {} } = X
     * asNewAsync { asNewReadOnly {} } = O
     * @return Deferred 타입 리턴이 리턴되면 동작안됨
     */
    suspend fun <T> withSlave(call: suspend () -> T): T {
        return coroutineScope {
            val fsContext: AsyncFsContext = findAsyncFsContext(coroutineContext)
            fsContext.incReadOnly()
            try {
                debugPrint("N-ReadOnly")
                if (fsContext.isReadOnly()) {
                    AsyncFsContext.CTX.setReadOnly()
                }
                call()
            } finally {
                fsContext.decReadOnly()
                if (!fsContext.isReadOnly()) {
                    AsyncFsContext.CTX.clearReadOnly()
                }
            }
        }
    }

//    private suspend fun getAsyncFsContext(): AsyncFsContext {
//        return withContext(getDefaultContext()) {
//            findAsyncFsContext(coroutineContext)
//        }
//        return coroutineScope {
//            findAsyncFsContext(coroutineContext)
//        }
//    }

    private fun findAsyncFsContext(context: CoroutineContext): AsyncFsContext {
        return context[AsyncFsContext] ?: context[ReactorContext]!!.context!![AsyncFsContext]
    }

//    private fun getDefaultContext() = FsDefaultContext() + threadPool

}