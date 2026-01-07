package com.miniorange.miniorange_android.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class MoAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "MoAccessibility"
        private var instance: MoAccessibilityService? = null
        
        /**
         * 检查服务是否已启用
         */
        fun isServiceEnabled(context: Context): Boolean {
            val expectedServiceName = context.packageName + "/" + 
                MoAccessibilityService::class.java.name
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            
            return enabledServicesSetting.contains(expectedServiceName)
        }
        
        /**
         * 获取服务实例
         */
        fun getInstance(): MoAccessibilityService? = instance
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "MoAccessibilityService 创建")
        
        // 注册广播接收器
        val filter = IntentFilter("MO_ACCESSIBILITY_COMMAND")
        registerReceiver(commandReceiver, filter)
        
        sendBroadcast("SERVICE_STARTED")
        sendLog("MiniOrange无障碍服务初始化完成")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "MoAccessibilityService 已连接")
        sendLog("服务连接成功，准备就绪")
        
        // 显示Toast提示
        handler.post {
            Toast.makeText(this, "MiniOrange自动化服务已启用", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowChanged(event)
            }
            
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
            }
            
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                handleViewFocused(event)
            }
            
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleTextChanged(event)
            }
            
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // 内容变化时自动扫描（防抖处理）
                if (!isScanning) {
                    handler.postDelayed({
                        scanCurrentScreen()
                    }, 300)
                    isScanning = true
                    handler.postDelayed({ isScanning = false }, 1000)
                }
            }
        }
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "MoAccessibilityService 被中断")
        sendLog("服务被系统中断")
        sendBroadcast("SERVICE_STOPPED")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MoAccessibilityService 销毁")
        instance = null
        unregisterReceiver(commandReceiver)
        sendLog("服务已停止运行")
    }
    
    /**
     * 处理窗口变化事件
     */
    private fun handleWindowChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: "unknown"
        val className = event.className?.toString() ?: "unknown"
        
        sendLog("窗口变化: $packageName")
        sendLog("类名: ${className.takeLast(50)}")
        
        // 如果是自己的应用
        if (packageName == this.packageName) {
            sendLog("检测到MiniOrange应用窗口")
        }
    }
    
    /**
     * 处理视图点击事件
     */
    private fun handleViewClicked(event: AccessibilityEvent) {
        val text = event.text?.firstOrNull()?.toString() ?: "无文本"
        val source = event.source
        
        sendLog("点击事件: ${text.take(50)}")
        
        source?.let { node ->
            // 记录节点信息
            logNodeInfo(node, "被点击")
            
            // 特定按钮的自动处理
            when {
                text.contains("测试") || text.contains("test") -> {
                    sendLog("检测到测试按钮，可添加自动化逻辑")
                }
                
                node.viewIdResourceName?.contains("btn_test") == true -> {
                    sendLog("检测到ID为btn_test的按钮被点击")
                    // 可以在这里添加自动化响应
                }
            }
            
            node.recycle()
        }
    }
    
    /**
     * 处理焦点事件
     */
    private fun handleViewFocused(event: AccessibilityEvent) {
        val text = event.text?.firstOrNull()?.toString() ?: "无文本"
        sendLog("焦点事件: ${text.take(30)}")
    }
    
    /**
     * 处理文本变化
     */
    private fun handleTextChanged(event: AccessibilityEvent) {
        val text = event.text?.firstOrNull()?.toString()
        val beforeText = event.beforeText?.firstOrNull()?.toString()
        
        if (text != null && text != beforeText) {
            sendLog("文本变化: '${beforeText}' -> '${text.take(30)}'")
        }
    }
    
    /**
     * 扫描当前屏幕所有节点
     */
    fun scanCurrentScreen() {
        val rootNode = rootInActiveWindow ?: run {
            sendLog("无法获取根节点")
            return
        }
        
        sendLog("开始扫描当前屏幕...")
        
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        val nodeInfo = StringBuilder()
        
        // 递归收集所有节点
        collectAllNodes(rootNode, nodes, 0)
        
        // 统计信息
        val clickableNodes = nodes.count { it.isClickable }
        val editableNodes = nodes.count { it.isEditable }
        val visibleNodes = nodes.count { it.isVisibleToUser }
        
        nodeInfo.append("=== 屏幕分析报告 ===\n")
        nodeInfo.append("总节点数: ${nodes.size}\n")
        nodeInfo.append("可点击节点: $clickableNodes\n")
        nodeInfo.append("可编辑节点: $editableNodes\n")
        nodeInfo.append("可见节点: $visibleNodes\n")
        nodeInfo.append("-----------------\n")
        
        // 显示前10个可点击节点
        nodes.take(10).forEachIndexed { index, node ->
            val text = node.text?.toString() ?: "无文本"
            val id = node.viewIdResourceName ?: "无ID"
            val className = node.className?.toString() ?: "未知类"
            
            if (text.isNotBlank() || id != "无ID") {
                nodeInfo.append("${index + 1}. $text\n    ID: $id\n    类: ${className.takeLast(30)}\n")
            }
        }
        
        if (nodes.size > 10) {
            nodeInfo.append("... 还有 ${nodes.size - 10} 个节点\n")
        }
        
        sendLog(nodeInfo.toString())
        sendNodeCount(nodes.size)
        
        // 回收所有节点
        nodes.forEach { it.recycle() }
        rootNode.recycle()
    }
    
    /**
     * 递归收集所有节点
     */
    private fun collectAllNodes(
        node: AccessibilityNodeInfo,
        result: MutableList<AccessibilityNodeInfo>,
        depth: Int
    ) {
        result.add(AccessibilityNodeInfo.obtain(node))
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            child?.let {
                collectAllNodes(it, result, depth + 1)
            }
        }
    }
    
    /**
     * 记录节点详细信息
     */
    private fun logNodeInfo(node: AccessibilityNodeInfo, action: String) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        
        val info = StringBuilder()
        info.append("节点信息 [$action]:\n")
        info.append("文本: ${node.text?.toString()?.take(50) ?: "空"}\n")
        info.append("ID: ${node.viewIdResourceName ?: "无"}\n")
        info.append("类: ${node.className?.toString() ?: "未知"}\n")
        info.append("坐标: [${bounds.left}, ${bounds.top}] - [${bounds.right}, ${bounds.bottom}]\n")
        info.append("尺寸: ${bounds.width()}x${bounds.height()}\n")
        info.append("可点击: ${node.isClickable}, 可编辑: ${node.isEditable}")
        
        sendLog(info.toString())
    }
    
    /**
     * 点击指定坐标
     */
    fun clickAt(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                .build()
            
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    sendLog("手势完成: 点击($x, $y)")
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    sendLog("手势取消: 点击($x, $y)")
                }
            }, null)
        } else {
            sendLog("当前Android版本不支持手势API")
        }
    }
    
    /**
     * 查找并点击包含指定文本的节点
     */
    fun clickByText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        var clicked = false
        
        for (node in nodes) {
            if (node.isClickable && node.isVisibleToUser) {
                sendLog("找到文本 '$text'，正在点击...")
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                clicked = true
                break
            }
        }
        
        nodes.forEach { it.recycle() }
        rootNode.recycle()
        
        return clicked
    }
    
    /**
     * 发送日志广播
     */
    private fun sendLog(message: String) {
        sendBroadcast("LOG:$message")
    }
    
    /**
     * 发送节点数量
     */
    private fun sendNodeCount(count: Int) {
        val intent = Intent("MO_ACCESSIBILITY_ACTION").apply {
            putExtra("message", "NODES:")
            putExtra("nodeCount", count)
            `package` = packageName
        }
        sendBroadcast(intent)
    }
    
    /**
     * 发送广播
     */
    private fun sendBroadcast(message: String) {
        val intent = Intent("MO_ACCESSIBILITY_ACTION").apply {
            putExtra("message", message)
            `package` = packageName
        }
        sendBroadcast(intent)
    }
    
    /**
     * 命令接收器
     */
    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra("command")) {
                "SCAN_SCREEN" -> scanCurrentScreen()
                "CLICK_BY_TEXT" -> {
                    val text = intent.getStringExtra("text") ?: ""
                    clickByText(text)
                }
                "CLICK_AT" -> {
                    val x = intent.getIntExtra("x", 0)
                    val y = intent.getIntExtra("y", 0)
                    clickAt(x, y)
                }
            }
        }
    }
}
