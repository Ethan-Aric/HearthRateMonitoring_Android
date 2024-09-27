plugins {
    id("com.android.application")
}

android {
    namespace = "com.lmx.heartbeatratemonitor"
    compileSdk = 34
    buildFeatures {
        // 启用数据绑定功能
        dataBinding=true
    }
    defaultConfig {
        applicationId = "com.lmx.heartbeatratemonitor"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation ("pub.devrel:easypermissions:3.0.0")
    // RxJava针对Android的扩展，处理Android线程调度等
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation ("com.github.Jasonchenlijian:FastBle:2.4.0")
    implementation ("com.github.AAChartModel:AAChartCore:cad1a66ee3")
    implementation ( "com.github.AnyChart:AnyChart-Android:1.1.5")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.0.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
//千万不要取消注释，会炸，别问我怎么知道的    implementation ("androidx.lifecycle:lifecycle-reactivestreams:2.8.2")
    testImplementation("junit:junit:4.13.2")
//这个也是    implementation("androidx.lifecycle:lifecycle-viewmodel-android:2.8.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

}