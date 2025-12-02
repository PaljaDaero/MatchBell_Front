plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

android {
    namespace = "com.example.matchbell"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.matchbell"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false //개발 완료시 삭제하기
            /*
            // ⬇️⬇️⬇️ 개발 완료시 주석 풀기 ⬇️⬇️⬇️
            // [보안] 코드를 난독화하여 역공학 방지 (false -> true 로 변경)
            isMinifyEnabled = true
            isShrinkResources = true // 안 쓰는 리소스 제거 (앱 용량도 줄어듦)
            */
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
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
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.core:core-ktx:1.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Retrofit (HTTP Client)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // JSON 파싱

    // OkHttp (WebSocket Client, Retrofit 내부적으로도 사용)
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // Google GSON (데이터 모델 직렬화/역직렬화)
    implementation ("com.google.code.gson:gson:2.10.1")

    // 원형 ImageView
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // 이미지 로딩 (선택 사항이지만 추천)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.gms:play-services-location:21.0.1")
}