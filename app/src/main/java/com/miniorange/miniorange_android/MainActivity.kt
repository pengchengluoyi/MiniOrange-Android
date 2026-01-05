package com.miniorange.miniorange_android

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. 加载布局文件
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btn_scan) // 需要在 layout 增加此 ID
        val txtStatus = findViewById<TextView>(R.id.txt_status)

        btnScan.setOnClickListener {
            GmsBarcodeScanning.getClient(this).startScan()
                .addOnSuccessListener { barcode ->
                    barcode.rawValue?.let { url ->
                        txtStatus.text = "已连接: $url"
                        SocketManager.connect(url)
                    }
                }
        }
    }
}