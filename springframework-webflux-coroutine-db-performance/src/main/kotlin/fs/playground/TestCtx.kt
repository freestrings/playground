package fs.playground

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class TestCtx(val uuid: String) :
    ThreadContextElement<Boolean>,
    AbstractCoroutineContextElement(TestCtx) {

    companion object Key : CoroutineContext.Key<TestCtx>

    override fun updateThreadContext(context: CoroutineContext): Boolean {
        println(uuid)
        return true
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean) {
    }
}