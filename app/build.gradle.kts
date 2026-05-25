plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")

    // phan duoi nay la them de ket noi voi firebase
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "huce.fit.myezticket"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "huce.fit.myezticket"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    //them de ket noi voi firebase
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-database") // Dùng cho QuestionnaireScreen

    // 2. Thêm các thư viện cần thiết cho App bán vé
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore") // Lưu dữ liệu sự kiện/vé
    implementation("com.google.firebase:firebase-auth")      // Đăng ký/Đăng nhập
    implementation("com.google.firebase:firebase-storage")   // Lưu ảnh poster

    //hỗ trợ Coroutines (suspend/await) cho Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Thêm thư viện hỗ trợ load ảnh từ mạng cho Compose (Rất quan trọng)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.zxing:core:3.5.3") // Dùng để tạo QR code trong PaymentMethodScreen

// 4. Lifecycle & ViewModel cho Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Tiện thể, nếu bạn dùng collectAsState, nên thêm cái này để tối ưu hiệu năng (năm 2026 nên có)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Dòng này giúp bạn dùng được tất cả các icon của Google
    implementation("androidx.compose.material:material-icons-extended")

    // Thư viện giúp chuyển màn hình
    implementation("androidx.navigation:navigation-compose:2.8.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Security Crypto (for encrypted shared preferences)
    implementation(libs.androidx.security.crypto)

    // Biometric
    implementation(libs.androidx.biometric)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}