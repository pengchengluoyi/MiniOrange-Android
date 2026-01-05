package com.miniorange.miniorange_android

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
                // 收到格式: {"action": "click", "x": 500, "y": 1000}
                if (data.optString("action") == "click") {
                    val x = data.optDouble("x").toFloat()
                    val y = data.optDouble("y").toFloat()
                    MService.instance?.performClick(x, y)
                }
            }
        })
    }
}