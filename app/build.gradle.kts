plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.webscannerapplication"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.webscannerapplication"
        minSdk = 26
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
    //Google AI SDK for Kotlin
    implementation(libs.generativeai)
    implementation(libs.androidx.security.crypto)
//    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
//    implementation("com.google.firebase:firebase-ai")
//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.ai)
//    implementation(libs.firebase.functions.ktx)

    //OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    //Jsoup
    implementation(libs.jsoup)
    //

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}