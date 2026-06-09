plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.newbeemall"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.newbeemall"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    // AndroidX 基础
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")

    // 轮播图
    implementation("io.github.youth5201314:banner:2.2.3")
    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // 测试
    testImplementation("junit:junit:4.13.2")
}
