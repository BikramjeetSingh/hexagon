package com.hexagonkt.http.server

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketsTest {

    @Test @Disabled fun `Create a basic WebSocket endpoint using the DSL`() {
        val router = Router {
            webSocket("/websocket") {
                onConnect {
                    send("hello")
                }
                onMessage { message ->
                    send(message)
                }
                onError { exception: Exception ->
                    print("Error $exception")
                }
                onClose {
                    send("goodbye")
                }
            }
        }
    }
}
