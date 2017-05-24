package co.there4.hexagon.events

import co.there4.hexagon.events.EventManager.consume
import co.there4.hexagon.events.EventManager.publish
import co.there4.hexagon.helpers.Log
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.nanoTime
import kotlin.reflect.KClass
import java.lang.Thread.`yield` as threadYield

@Test class EventsTest {
    class TickEvent (val nanos: Long) : Event ()

    object VoidEngine : EventEngine {
        var registry: Map<KClass<out Event>, (Event) -> Unit> = mapOf()

        override fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
            @Suppress("UNCHECKED_CAST")
            registry += type to (consumer as (Event) -> Unit)
        }

        override fun publish(event: Event, address: String) {
            registry[event.javaClass.kotlin]?.invoke(event)
        }
    }

    private var tick: Long = 0

    @BeforeClass fun startConsumer() {
        EventManager.engine = VoidEngine

        consume(TickEvent::class) {
            Log.info("Tick: ${it.nanos}")
            tick = it.nanos
        }
    }

    fun events_are_published_properly() {
        val nanos = nanoTime()
        publish(TickEvent(nanos))
        assert(tick == nanos)
    }
}
