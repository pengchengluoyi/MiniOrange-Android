package com.miniorange.miniorange_android.accessibility

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.miniorange.miniorange_android.R
import java.text.SimpleDateFormat
import java.util.*

class AccessibilityDemoActivity : AppCompatActivity() {
    
    private lateinit var tvServiceStatus: TextView
    private lateinit var tvLogs: TextView
    private lateinit var tvNodeCount: TextView
    
    private val logDateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility_demo)
        
        initViews()
        setupClickListeners()
        updateServiceStatus()
        
        // 注册广播接收器
        registerReceiver(accessibilityBroadcastReceiver, IntentFilter("MO_ACCESSIBILITY_ACTION"))
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(accessibilityBroadcastReceiver)
    }
    
    private fun initViews() {
        tvServiceStatus = findViewById(R.id.tvServiceStatus)
        tvLogs = findViewById(R.id.tvLogs)
        tvNodeCount = findViewById(R.id.tvNodeCount)
        
        // 设置日志TextView可滚动
        tvLogs.movementMethod = ScrollingMovementMethod()
    }
    
    private fun setupClickListeners() {
        // 打开无障碍设置
        findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
            openAccessibilitySettings()
        }
        
        // 检查服务状态
        findViewById<Button>(R.id.btnCheckStatus).setOnClickListener {
            updateServiceStatus()
            addLog("手动检查服务状态")
        }
        
        // 测试按钮点击（会被无障碍服务检测到）
        findViewById<Button>(R.id.btn_test).setOnClickListener {
            addLog("用户点击了测试按钮 (ID: btn_test)")
            Toast.makeText(this, "测试按钮被点击", Toast.LENGTH_SHORT).show()
        }
        
        // 模拟返回键
        findViewById<Button>(R.id.btnSimulateBack).setOnClickListener {
            if (MoAccessibilityService.isServiceEnabled(this)) {
                MoAccessibilityService.getInstance()?.performGlobalAction(
                    android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
                )
                addLog("已发送模拟返回键指令")
            } else {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 模拟主页键
        findViewById<Button>(R.id.btnSimulateHome).setOnClickListener {
            if (MoAccessibilityService.isServiceEnabled(this)) {
                MoAccessibilityService.getInstance()?.performGlobalAction(
                    android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
                )
                addLog("已发送模拟主页键指令")
            } else {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 扫描屏幕
        findViewById<Button>(R.id.btnScanScreen).setOnClickListener {
            if (MoAccessibilityService.isServiceEnabled(this)) {
                MoAccessibilityService.getInstance()?.scanCurrentScreen()
                addLog("已发送屏幕扫描指令")
            } else {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 清除日志
        findViewById<Button>(R.id.btnClearLogs).setOnClickListener {
            tvLogs.text = "MiniOrange 无障碍服务日志\n=====================\n"
            addLog("日志已清除")
        }
    }
    
    /**
     * 打开无障碍设置页面
     */
    private fun openAccessibilitySettings() {
        try {
            addLog("正在跳转到无障碍设置...")
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            addLog("打开设置失败: ${e.message}")
            Toast.makeText(this, "无法打开无障碍设置", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 更新服务状态显示
     */
    private fun updateServiceStatus() {
        val isEnabled = MoAccessibilityService.isServiceEnabled(this)
        val statusText = if (isEnabled) {
            "✅ 服务状态: 已启用 (MiniOrange Automation)"
        } else {
            "❌ 服务状态: 未启用 - 点击上方按钮开启"
        }
        tvServiceStatus.text = statusText
    }
    
    /**
     * 添加日志到TextView
     */
    fun addLog(message: String) {
        runOnUiThread {
            val time = logDateFormat.format(Date())
            val logEntry = "[$time] $message\n"
            tvLogs.append(logEntry)
            
            // 自动滚动到底部
            val layout = tvLogs.layout
            if (layout != null) {
                val scrollAmount = layout.getLineTop(tvLogs.lineCount) - tvLogs.height
                if (scrollAmount > 0) {
                    tvLogs.scrollTo(0, scrollAmount)
                }
            }
        }
    }
    
    /**
     * 更新节点计数
     */
    fun updateNodeCount(count: Int) {
        runOnUiThread {
            tvNodeCount.text = "当前屏幕控件数: $count"
        }
    }
    
    /**
     * 广播接收器，接收来自无障碍服务的消息
     */
    private val accessibilityBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "MO_ACCESSIBILITY_ACTION" -> {
                    val message = intent.getStringExtra("message") ?: ""
                    val nodeCount = intent.getIntExtra("nodeCount", 0)
                    
                    when {
                        message.startsWith("LOG:") -> addLog(message.removePrefix("LOG:"))
                        message.startsWith("NODES:") -> updateNodeCount(nodeCount)
                        message == "SERVICE_STARTED" -> {
                            updateServiceStatus()
                            addLog("MiniOrange无障碍服务已启动")
                            Toast.makeText(this@AccessibilityDemoActivity, 
                                "服务启动成功", Toast.LENGTH_SHORT).show()
                        }
                        message == "SERVICE_STOPPED" -> {
                            updateServiceStatus()
                            addLog("服务已停止")
                        }
                    }
                }
            }
        }
    }
    
    companion object {
        /**
         * 启动演示Activity
         */
        fun start(context: Context) {
            val intent = Intent(context, AccessibilityDemoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
