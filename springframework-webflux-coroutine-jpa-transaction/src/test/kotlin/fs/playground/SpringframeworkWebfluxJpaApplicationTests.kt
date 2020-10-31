package fs.playground

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

@SpringBootTest
class SpringframeworkWebfluxJpaApplicationTests {

    class Testa {
        private val dispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

        private val counter = AtomicInteger(0)
        private val errorCounter = AtomicInteger(0)

        fun a(msg: String, expected: State? = null) {
            try {
                TT.assert(expected)
            } catch (e: Throwable) {
                println("error[$expected]: $msg")
                errorCounter.incrementAndGet()
            }

        }

        fun doAsync(call: suspend () -> Unit) {
            counter.incrementAndGet()
            CoroutineScope(MyContext("doAsync") + dispatcher).async {
                try {
                    call()
                } finally {
                    counter.decrementAndGet()
                }
            }
        }

        suspend fun inBlock(call: suspend () -> Unit) {
            withContext(TT.inBlock()) {
                call()
            }
        }

        suspend fun normal(call: suspend () -> Unit) {
            withContext(MyContext("normal")) {
                call()
            }
        }

        fun run() {
            while (counter.get() != 0) {
                Thread.sleep(1000)
                if (errorCounter.get() > 0) {
                    throw Exception("fail")
                }
            }
        }
    }

    suspend fun blockDefault(i: Int, testa: Testa, message: String? = "0") {
        testa.inBlock {
            testa.a("$i.blockDefault.1.$message", State.IN_BLOCK)

            testa.doAsync {
                testa.a("$i.blockDefault.2.$message", State.IN_BLOCK)
            }

            testa.doAsync {
                testa.a("$i.blockDefault.3.$message", State.IN_BLOCK)
            }

            testa.a("$i.blockDefault.4.$message", State.IN_BLOCK)
        }
    }

    suspend fun normal(i: Int, testa: Testa, message: String? = "0") {
        testa.a("$i.normal.1.$message")

        testa.doAsync {
            testa.a("$i.normal.2.$message")
        }
    }

    fun defaultTesta(i: Int, testa: Testa) {

        testa.doAsync {

            blockDefault(i, testa)

            normal(i, testa)

            testa.inBlock {
                blockDefault(i, testa, "1")

                testa.normal {
                    normal(i, testa, "1")
                }

                blockDefault(i, testa, "2")
            }

            normal(i, testa, "2")
        }
    }

    @Test
    fun iterMany() {
        val testa = Testa()
        runBlocking {
            for (i in 0 until 10) {
                defaultTesta(i, testa)
            }
        }
        testa.run()
    }

    enum class State {
        IN_BLOCK,
    }

    object TT {

        private val localThread = ThreadLocal<State?>()

        fun get(): State? {
            return localThread.get()
        }

        fun set(value: State?) {
            localThread.set(value)
        }

        fun remove() {
            localThread.remove()
        }

        fun assert(state: State?) {
            Assertions.assertEquals(state, get())
        }

        fun inBlock() = localThread.asContextElement(State.IN_BLOCK)

    }

    class MyContext(val message: String) : ThreadContextElement<State?>, AbstractCoroutineContextElement(MyContext) {
        companion object Key : CoroutineContext.Key<MyContext>

        private val data = TT.get()

        override fun updateThreadContext(context: CoroutineContext): State? {
            val old = TT.get()
            TT.set(data)
            return old
        }

        override fun restoreThreadContext(context: CoroutineContext, oldState: State?) {
            when (oldState) {
                State.IN_BLOCK -> TT.set(oldState)
                else -> TT.remove()
            }
        }
    }
}
