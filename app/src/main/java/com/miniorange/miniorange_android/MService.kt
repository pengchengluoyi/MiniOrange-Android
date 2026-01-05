package com.miniorange.miniorange_android
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class MService : AccessibilityService() {
    companion object { var instance: MService? = null }
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    // 统一执行手势逻辑
    fun executeGesture(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val builder = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(builder, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}