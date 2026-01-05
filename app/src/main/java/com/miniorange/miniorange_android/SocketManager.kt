package com.miniorange.miniorange_android
import okhttp3.*
import org.json.JSONObject

object SocketManager {
    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        OkHttpClient().newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                // 远程指令格式: {"action": "click", "x": 500, "y": 1000}
                if (json.optString("action") == "click") {
                    val x = json.optDouble("x").toFloat()
                    val y = json.optDouble("y").toFloat()
                    // 修复此处：调用升级后的通用手势方法，点击时长设为 100ms
                    MService.instance?.executeGesture(x, y, x, y, 100)
                }
            }
        })
    }
}