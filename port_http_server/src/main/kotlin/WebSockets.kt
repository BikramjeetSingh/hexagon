package com.hexagonkt.http.server

class WebSocketContext {
    fun send(content: Any) {

    }
}

class WebSocket {

    private lateinit var context: WebSocketContext

    fun onConnect(block: WebSocketContext.() -> Unit) {
        context = WebSocketContext().apply(block)
    }

    fun onMessage(block: WebSocketContext.(Any) -> Unit) {

    }

    fun onError(block: WebSocketContext.(Exception) -> Unit) {

    }

    fun onClose(block: WebSocketContext.() -> Unit) {

    }
}
