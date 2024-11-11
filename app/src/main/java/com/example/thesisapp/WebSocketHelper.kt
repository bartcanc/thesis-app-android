package com.example.thesisapp

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketHelper(private val url: String) {

    private var webSocket: WebSocket? = null

    fun startConnection() {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: okhttp3.Response) {
                Log.d("WebSocket", "Połączenie WebSocket zostało nawiązane")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocket", "Odebrano wiadomość: $text")
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Odebrano dane binarne: ${bytes.hex()}")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WebSocket", "Błąd połączenia: ${t.message}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Połączenie WebSocket zostało zamknięte: $reason")
            }
        })
    }

    fun stopConnection() {
        webSocket?.close(1000, "Zamknięcie połączenia przez użytkownika")
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
}
