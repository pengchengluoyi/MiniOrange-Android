package com.miniorange.miniorange_android
import okhttp3.*
import org.json.JSONObject

object SocketManager {
    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        OkHttpClient().newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                if (json.optString("action") == "click") {
                    MService.instance?.performClick(
                        json.optDouble("x").toFloat(),
                        json.optDouble("y").toFloat()
                    )
                }
            }
        })
    }
}