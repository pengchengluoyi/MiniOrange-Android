package com.miniorange.miniorange_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 申请相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        // 引导无障碍
        checkAccessibility()

        val editCoords = findViewById<EditText>(R.id.edit_coords)

        // 本地点击测试 (持续100ms)
        findViewById<Button>(R.id.test_click).setOnClickListener {
            val pts = parseCoords(editCoords.text.toString())
            MService.instance?.executeGesture(pts[0], pts[1], pts[0], pts[1], 100)
        }

        // 本地滑动测试 (持续500ms)
        findViewById<Button>(R.id.test_swipe).setOnClickListener {
            val pts = parseCoords(editCoords.text.toString())
            MService.instance?.executeGesture(pts[0], pts[1], pts[2], pts[3], 500)
        }

        // 本地拖拽测试 (持续2000ms，模拟长按后移动)
        findViewById<Button>(R.id.test_drag).setOnClickListener {
            val pts = parseCoords(editCoords.text.toString())
            MService.instance?.executeGesture(pts[0], pts[1], pts[2], pts[3], 2000)
        }

        // 扫码功能
        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            GmsBarcodeScanning.getClient(this).startScan().addOnSuccessListener {
                it.rawValue?.let { url -> SocketManager.connect(url) }
            }
        }
    }

    private fun parseCoords(input: String): FloatArray {
        return try {
            input.split(",").map { it.trim().toFloat() }.toFloatArray()
        } catch (e: Exception) {
            floatArrayOf(500f, 1000f, 500f, 500f)
        }
    }

    private fun checkAccessibility() {
        val serviceId = "${packageName}/${packageName}.MService"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (enabled == null || !enabled.contains(serviceId)) {
            android.app.AlertDialog.Builder(this)
                .setTitle("开启无障碍")
                .setMessage("请在随后的设置中找到 [MiniOrange助手] 并开启")
                .setPositiveButton("去开启") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }.show()
        }
    }
}