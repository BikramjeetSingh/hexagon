package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.events.rabbitmq.RabbitMqClient.Companion.createConnectionFactory
import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.serialization.serialize
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertFailsWith

@Test class RabbitMqClientTest {
    companion object : CachedLogger(RabbitMqClientTest::class)

    fun create_a_connection_factory_with_empty_URI_fails() {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory(URI(""))
        }
    }

    fun create_a_connection_factory_with_invalid_URI_fails() {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory(URI("http://localhost"))
        }
    }

    fun create_a_connection_factory_without_parameters_succeed() {
        val uri = "amqp://user:pass@localhost:12345"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun create_a_connection_factory_with_one_parameter_succeed() {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun create_a_connection_factory_with_two_parameter_succeed() {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50&heartbeat=25"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun create_a_connection_factory_with_all_parameters_succeed() {
        val opt = "channelCacheSize=50&heartbeat=25&automaticRecovery=true&recoveryInterval=5"
        val uri = "amqp://user:pass@localhost:12345?$opt"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun rabbit_client_disconnects_properly() {
        val client = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        assert(client.connected)
        client.close()
        assert(!client.connected)
        assertFailsWith<IllegalStateException> {
            client.close()
        }
    }

    fun consumers_handle_numbers_properly() {
        val consumer = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        consumer.declareQueue("int_op")
        consumer.declareQueue("long_op")
        consumer.declareQueue("list_op")
        consumer.consume("int_op", String::class, String::toInt)
        consumer.consume("long_op", String::class, String::toLong)
        consumer.consume("list_op", List::class) { it }

        val client = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        assert(client.call("int_op", "123") == "123")
        assert(client.call("long_op", "456") == "456")
        assert(client.call("list_op", listOf(1, 3, 4).serialize()) == listOf(1, 3, 4).serialize())

        client.close()
        consumer.deleteQueue("int_op")
        consumer.deleteQueue("long_op")
        consumer.deleteQueue("list_op")
        consumer.close()
    }

    fun consumers_handle_no_reply_messages() {
        val consumer = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        consumer.declareQueue("int_handler")
        consumer.declareQueue("long_handler")
        consumer.declareQueue("exception_handler")
        consumer.consume("int_handler", String::class) { info(it) }
        consumer.consume("long_handler", String::class) { info(it) }
        consumer.consume("exception_handler", String::class) { throw RuntimeException(it) }

        val client = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        client.publish("int_handler", "123")
        client.publish("long_handler", "456")
        client.publish("exception_handler", "error")
        client.publish("exception_handler", "")

        client.close()
        consumer.deleteQueue("int_handler")
        consumer.deleteQueue("long_handler")
        consumer.deleteQueue("exception_handler")
        consumer.close()
    }
}