package com.miniorange.assistant

import okhttp3.*
import org.json.JSONObject

object SocketManager {
    private val client = OkHttpClient()
    private var ws: WebSocket? = null

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val data = JSONObject(text)
                // 假设后端发送 {"action": "click", "x": 500, "y": 1000}
                if (data.getString("action") == "click") {
                    val x = data.getDouble("x").toFloat()
                    val y = data.getDouble("y").toFloat()
                    MService.instance?.performClick(x, y)
                }
            }
        })
    }
}