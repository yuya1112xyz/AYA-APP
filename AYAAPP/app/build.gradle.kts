// AYA-APP/app/build.gradle.kts
plugins {
    id("com.android.application")
    kotlin("android")
    //kotlin("kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.compose")
}

android {
    namespace = "com.example.ayaapp"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.ayaapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    // ★ Java のターゲットを 17 に統一（1.8 → 17 へ）
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // （ComposeやbuildTypesなどはそのままでOK）
    buildFeatures { compose = true }
    //composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }


}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity / Lifecycle
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Room（KAPT 使用）

    implementation("androidx.room:room-ktx:2.6.1")
    // kapt("androidx.room:room-compiler:2.6.1") ← 削除
    ksp("androidx.room:room-compiler:2.6.1")


    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Permissions（Compose）
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // ★ 追加：Material Components（XMLスタイルの Theme.Material3.* を提供）
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material:material-icons-extended")

}


// ★ Kotlin 2.0 の推奨記法：Kotlin 全タスクの JVM ターゲットとツールチェーンを 17 に固定
kotlin {
    // Kotlin コンパイラの JVM ターゲットを 17 に
    compilerOptions {
        // import org.jetbrains.kotlin.gradle.dsl.JvmTarget
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    // Kotlin ツールチェーンも JDK 17 を使用
    jvmToolchain(17)
}
