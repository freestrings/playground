package fs.playground.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestaEventHandler {

    @EventListener
    fun testaEvent(event: TestaEvent) {
        Thread.sleep(5000)
        println("handler $event - ${Thread.currentThread()}")
    }
}