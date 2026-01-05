package com.miniorange.miniorange_android

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanner = GmsBarcodeScanning.getClient(this)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.let { url ->
                    SocketManager.connect(url)
                    Toast.makeText(this, "正在连接至: $url", Toast.LENGTH_SHORT).show()
                }
            }
    }
}