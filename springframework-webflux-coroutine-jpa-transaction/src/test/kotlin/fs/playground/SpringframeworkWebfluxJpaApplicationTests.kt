package fs.playground

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Executors
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


@SpringBootTest
class SpringframeworkWebfluxJpaApplicationTests {

    class Testa {
        private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

        val counter = AtomicInteger(0)

        suspend fun doAsync(msg: String, expected: State) {
            counter.incrementAndGet()
            CoroutineScope(MyContext(null, "doAsync") + dispatcher).async {
                try {
                    TT.assert(expected)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                counter.decrementAndGet()
                println("@ call $msg : ${TT.get()}, ${Thread.currentThread().name}")
                println("# remain $counter")
            }
        }

        fun doAsyncCall(call: suspend () -> Unit) {
            CoroutineScope(MyContext(null, "doAsyncCall") + dispatcher).async {
                call()
            }
        }

        fun run() {
            Thread.sleep(1000)
            while (counter.get() != 0) {
                Thread.sleep(1000)
                println("remain $counter")
            }
        }
    }

    fun iter1(i: Int, testa: Testa) {
        testa.doAsyncCall {
            withContext(MyContext(State.IN_BLOCK, "$i.1")) {
                testa.doAsync("$i.1", State.IN_BLOCK)
//                testa.doAsyncCall {
//                    testa.doAsync("$i.2", State.IN_BLOCK)
//                }
//                withContext(MyContext(State.UNKNOWN)) {
//                    testa.doAsync("$i.3", State.UNKNOWN)
//                    testa.doAsync("$i.4", State.UNKNOWN)
//                    withContext(MyContext(State.IN_BLOCK)) {
//                        testa.doAsync("$i.5", State.IN_BLOCK)
//                        testa.doAsync("$i.6", State.IN_BLOCK)
//                    }
//                }
//                withContext(MyContext(State.UNKNOWN)) {
//                    testa.doAsync("7", State.UNKNOWN)
//                    testa.doAsync("8", State.UNKNOWN)
//                    withContext(MyContext(State.IN_BLOCK)) {
//                        testa.doAsync("9", State.IN_BLOCK)
//                        testa.doAsync("10", State.IN_BLOCK)
//                    }
//                }
            }
            testa.doAsync("$i.11", State.IN_BLOCK)
//            withContext(MyContext(State.IN_BLOCK)) {
//                testa.doAsync("12", State.IN_BLOCK)
//                testa.doAsync("13", State.IN_BLOCK)
//            }
//            testa.doAsyncCall {
//                testa.doAsync("14", State.IN_BLOCK)
//                withContext(MyContext(State.UNKNOWN)) {
//                    testa.doAsync("15", State.UNKNOWN)
//                    testa.doAsync("16", State.UNKNOWN)
//                }
//            }
//            testa.doAsync("17", State.UNKNOWN)
//            testa.doAsync("18", State.UNKNOWN)
//            testa.doAsync("19", State.UNKNOWN)
//            testa.doAsync("20", State.UNKNOWN)
        }
    }

    @Test
    fun iterMany() {
        runBlocking {
            val testa = Testa()
            for (i in 0 until 2) {
                iter1(i, testa)
            }
            testa.run()
        }
    }

    enum class State {
        UNKNOWN,
        IN_BLOCK,
    }

    object TT {

        private val localThread = object : ThreadLocal<State?>() {
            override fun initialValue(): State {
                return State.UNKNOWN
            }
        }

        fun get(): State? {
            return localThread.get()
        }

        fun set(value: State?) {
            localThread.set(value)
        }

        fun remove() {
            localThread.remove()
        }

        fun assert(state: State) {
            println("$state, ${get()}")
            Assertions.assertEquals(state, get())
        }

    }

    class MyContext(state: State?, val msg: String) : ThreadContextElement<State?>, AbstractCoroutineContextElement(MyContext) {
        companion object Key : CoroutineContext.Key<MyContext>

        private val data = state?.let { state } ?: TT.get()

        init {
            println("^ $data - $msg")
        }

        override fun updateThreadContext(context: CoroutineContext): State? {
            val old = TT.get()
            TT.set(data)
            return old
        }

        override fun restoreThreadContext(context: CoroutineContext, oldState: State?) {
            println("restore $msg, $oldState")
            when (oldState) {
                State.IN_BLOCK -> TT.set(oldState)
                else -> TT.remove()
            }
        }
    }
}
