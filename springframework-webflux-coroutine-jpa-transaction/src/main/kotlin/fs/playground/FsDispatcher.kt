package fs.playground

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import reactor.core.scheduler.Schedulers
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

class AsyncFsContext(val uuid: String, private var readonly: Int = 0) :
        ThreadContextElement<Boolean>,
        AbstractCoroutineContextElement(AsyncFsContext) {

    companion object Key : CoroutineContext.Key<AsyncFsContext>

    override fun updateThreadContext(context: CoroutineContext): Boolean {
        debugPrint("Update", uuid, readonly.toString())
        val readonly = CTX.isReadOnly()
        CTX.setUuid(uuid)
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

        CTX.setUuid(uuid)
    }

    fun incReadOnly() {
        readonly++
        debugPrint("INC", uuid, readonly.toString())
    }

    fun decReadOnly() {
        readonly--
        debugPrint("DEC", uuid, readonly.toString())
    }

    fun isReadOnly() = readonly != 0

    object CTX {

        private val readOnly = ThreadLocal<String?>()
        private val uuid = ThreadLocal<String?>()

        fun setReadOnly() {
            debugPrint("Set")
            readOnly.set("true")
        }

        fun isReadOnly() = readOnly.get() == "true"

        fun clearReadOnly() {
            debugPrint("Clear")
            readOnly.remove()
        }

        fun setUuid(value: String) {
            uuid.set(value)
        }

        fun clearUuid() {
            uuid.remove()
        }

        fun getUuid() = uuid.get()

    }
}

object FsDispatcher {

    private val threadPool = Schedulers.boundedElastic().asCoroutineDispatcher()

    suspend fun <T> asFsContext(call: suspend () -> T): T {
        return withContext(getAsyncFsContext()) {
            call()
        }
    }

    suspend fun <T> asNewAsync(call: suspend () -> T): Deferred<T> {
        val fsContext = getAsyncFsContext()
        val isReadOnly = fsContext.isReadOnly()
        return CoroutineScope(fsContext + getDefaultContext()).async {
            if (isReadOnly) {
                debugPrint("NewAsync")
                AsyncFsContext.CTX.setReadOnly()
            }
            call()
        }
    }

    /**
     * asNewReadOnly { asNewAsync {} } = X
     * asNewAsync { asNewReadOnly {} } = O
     * @return Deferred 타입 리턴이 리턴되면 동작안됨
     */
    suspend fun <T> asNewReadOnly(call: suspend () -> T): T {
        return withContext(getAsyncFsContext()) {
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

    private suspend fun getAsyncFsContext(): AsyncFsContext {
        return withContext(getDefaultContext()) {
            findAsyncFsContext(coroutineContext)
        }
    }

    private fun findAsyncFsContext(context: CoroutineContext): AsyncFsContext {
        return context[AsyncFsContext] ?: context[ReactorContext]!!.context!![AsyncFsContext]
    }

    private fun getDefaultContext() = FsDefaultContext() + threadPool

}