package com.suda.agent.network

import android.util.Log
import kotlinx.coroutines.channels.Channel
import okhttp3.*

class WSClient : WebSocketListener() {

    private var webSocket: WebSocket? = null
    val incoming = Channel<String>(Channel.BUFFERED)

    fun connect(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, this)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        incoming.trySend(text)
    }

    fun send(text: String) {
        webSocket?.send(text)
    }

    fun close() {
        webSocket?.close(1000, null)
    }
}