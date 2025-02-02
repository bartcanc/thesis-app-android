package com.example.thesisapp

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WebSocketHelper(
    private val context: Context,
    private val userId: String?,
    private val sessionId: String?
) {

    private val client = HttpClient {
        install(WebSockets)
    }

    private val url: String = "ws://192.168.108.97:8000/socket/connect/$userId"

    private var job: Job? = null

    fun startConnection() {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket({
                    url(this@WebSocketHelper.url)
                    headers {
                        append("Sec-WebSocket-Protocol", "session-id.$sessionId")
                    }
                }) {
                    Log.d("WebSocket", "Połączenie WebSocket zostało nawiązane")

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val message = frame.readText()
                                Log.d("WebSocket", "Odebrano wiadomość: $message")
                                showAlert(message) // Wywołanie funkcji wyświetlającej alert
                            }
                            is Frame.Binary -> {
                                Log.d("WebSocket", "Odebrano dane binarne")
                            }
                            else -> {
                                Log.d("WebSocket", "Nieobsługiwany typ ramki")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Błąd połączenia: ${e.message}")
            }
        }
    }

    fun stopConnection() {
        job?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            client.close()
            Log.d("WebSocket", "Połączenie WebSocket zostało zamknięte")
        }
    }

    private fun showAlert(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (context is Activity && !context.isFinishing && !context.isDestroyed) {
                AlertDialog.Builder(context)
                    .setTitle("New Message")
                    .setMessage("Trening został zaspisany, możesz dodać kolejny trening!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                Log.w("WebSocketHelper", "Context is not valid for showing AlertDialog")
            }
        }
    }

}
