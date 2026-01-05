package com.miniorange.miniorange_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 启动即申请相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        // 2. 引导开启无障碍服务
        checkAccessibility()

        val editCoords = findViewById<EditText>(R.id.edit_coords)

        // 本地测试：点击 (100ms)
        findViewById<Button>(R.id.test_click).setOnClickListener {
            val p = parse(editCoords.text.toString())
            MService.instance?.executeGesture(p[0], p[1], p[0], p[1], 100)
        }

        // 本地测试：滑动 (500ms)
        findViewById<Button>(R.id.test_swipe).setOnClickListener {
            val p = parse(editCoords.text.toString())
            MService.instance?.executeGesture(p[0], p[1], p[2], p[3], 500)
        }

        // 本地测试：拖拽 (2000ms)
        findViewById<Button>(R.id.test_drag).setOnClickListener {
            val p = parse(editCoords.text.toString())
            MService.instance?.executeGesture(p[0], p[1], p[2], p[3], 2000)
        }

        // 远程扫码
        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            GmsBarcodeScanning.getClient(this).startScan().addOnSuccessListener {
                it.rawValue?.let { url -> SocketManager.connect(url) }
            }
        }
    }

    private fun parse(s: String): FloatArray {
        return try { s.split(",").map { it.trim().toFloat() }.toFloatArray() }
        catch (e: Exception) { floatArrayOf(500f, 1500f, 500f, 500f) }
    }

    private fun checkAccessibility() {
        val serviceId = "${packageName}/${MService::class.java.name}"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (enabled == null || !enabled.contains(serviceId)) {
            android.app.AlertDialog.Builder(this)
                .setTitle("需要开启无障碍")
                .setMessage("请找到 [MiniOrange助手] 并开启，否则测试按钮无效")
                .setPositiveButton("去开启") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }.show()
        }
    }
}