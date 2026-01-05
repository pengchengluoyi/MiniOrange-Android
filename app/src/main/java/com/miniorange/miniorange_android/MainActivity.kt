package com.miniorange.miniorange_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {
    private val REQ_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 修复黑屏

        // 1. 启动即检查并申请相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CODE)
        } else {
            checkAccessibility()
        }

        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            startScanner()
        }
    }

    private fun checkAccessibility() {
        val serviceId = "${packageName}/${MService::class.java.name}"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (enabled == null || !enabled.contains(serviceId)) {
            // 引导开启无障碍
            android.app.AlertDialog.Builder(this)
                .setTitle("需要无障碍权限")
                .setMessage("请在设置中开启 [MiniOrange助手] 以执行自动化点击")
                .setPositiveButton("去开启") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }.show()
        }
    }

    private fun startScanner() {
        GmsBarcodeScanning.getClient(this).startScan().addOnSuccessListener {
            it.rawValue?.let { url -> SocketManager.connect(url) }
        }
    }
}