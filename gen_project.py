import os

project_name = "MiniOrange-Android"
base_path = os.path.join(os.getcwd(), project_name)

# 定义需要创建的目录结构
dirs = [
    "app/src/main/java/com/miniorange/assistant",
    "app/src/main/res/xml",
    "app/src/main/res/values",
    ".github/workflows",
    "gradle/wrapper"
]

for d in dirs:
    os.makedirs(os.path.join(base_path, d), exist_ok=True)

# 1. 根目录 build.gradle
with open(os.path.join(base_path, "build.gradle"), "w", encoding="utf-8") as f:
    f.write("""plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
}""")

# 2. settings.gradle
with open(os.path.join(base_path, "settings.gradle"), "w", encoding="utf-8") as f:
    f.write("""rootProject.name = "MiniOrange-Android"
include ':app'""")

# 3. app/build.gradle
with open(os.path.join(base_path, "app/build.gradle"), "w", encoding="utf-8") as f:
    f.write("""plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}
android {
    namespace 'com.miniorange.assistant'
    compileSdk 34
    defaultConfig {
        applicationId "com.miniorange.assistant"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
}
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.android.gms:play-services-code-scanner:16.1.0'
}""")

# 4. AndroidManifest.xml
with open(os.path.join(base_path, "app/src/main/AndroidManifest.xml"), "w", encoding="utf-8") as f:
    f.write("""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <application android:label="MiniOrange助手" android:theme="@style/Theme.AppCompat.Light">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <service android:name=".MService" android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE" android:exported="true">
            <intent-filter><action android:name="android.view.accessibility.AccessibilityService" /></intent-filter>
            <meta-data android:name="android.accessibilityservice" android:resource="@xml/accessibility_service_config" />
        </service>
    </application>
</manifest>""")

# 5. MService.kt
with open(os.path.join(base_path, "app/src/main/java/com/miniorange/assistant/MService.kt"), "w", encoding="utf-8") as f:
    f.write("""package com.miniorange.assistant
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
class MService : AccessibilityService() {
    companion object { var instance: MService? = null }
    override fun onServiceConnected() { instance = this }
    fun performClick(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 100)).build()
        dispatchGesture(gesture, null, null)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}""")

# 6. SocketManager.kt
with open(os.path.join(base_path, "app/src/main/java/com/miniorange/assistant/SocketManager.kt"), "w", encoding="utf-8") as f:
    f.write("""package com.miniorange.assistant
import okhttp3.*
import org.json.JSONObject
object SocketManager {
    private val client = OkHttpClient()
    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val data = JSONObject(text)
                if (data.getString("action") == "click") {
                    MService.instance?.performClick(data.getDouble("x").toFloat(), data.getDouble("y").toFloat())
                }
            }
        })
    }
}""")

# 7. MainActivity.kt
with open(os.path.join(base_path, "app/src/main/java/com/miniorange/assistant/MainActivity.kt"), "w", encoding="utf-8") as f:
    f.write("""package com.miniorange.assistant
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GmsBarcodeScanning.getClient(this).startScan().addOnSuccessListener { barcode ->
            barcode.rawValue?.let { SocketManager.connect(it) }
        }
    }
}""")

# 8. accessibility_service_config.xml
with open(os.path.join(base_path, "app/src/main/res/xml/accessibility_service_config.xml"), "w", encoding="utf-8") as f:
    f.write("""<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:canPerformGestures="true"
    android:description="@string/service_desc" />""")

# 9. strings.xml
with open(os.path.join(base_path, "app/src/main/res/values/strings.xml"), "w", encoding="utf-8") as f:
    f.write("""<resources><string name="service_desc">MiniOrange 自动化执行引擎</string></resources>""")

# 10. workflow.yml (已升级为 v4)
with open(os.path.join(base_path, ".github/workflows/workflow.yml"), "w", encoding="utf-8") as f:
    f.write("""name: Android CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Setup Gradle
        run: |
          gradle wrapper --gradle-version 8.2
          chmod +x gradlew
      - name: Build APK
        run: ./gradlew assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: MiniOrange-Assistant
          path: app/build/outputs/apk/debug/app-debug.apk""")

print(f"项目已生成在: {base_path}")
print("请进入该目录并执行 git push 即可自动打包。")