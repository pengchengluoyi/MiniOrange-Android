plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.miniorange.miniorange_android"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.miniorange.miniorange_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 3    // 每次更新版本加 1
        versionName = "1.0.3" // 修改为你想要的版本号

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 基础库
    implementation("androidx.appcompat:appcompat:1.6.1")
    // WebSocket 通讯
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // 谷歌扫码服务
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
}