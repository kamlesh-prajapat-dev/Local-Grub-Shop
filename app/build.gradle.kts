import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs)

    // Firebase plugin
    id("com.google.gms.google-services")

    // Ksp plugin and Dagger hilt plugin
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.hiltAndroid)

    // Kotlin Serialization Plugin
    kotlin("plugin.serialization") version "2.2.21"
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.localgrubshop"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.localgrubshop"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose CLOUDINARY_CLOUD_NAME to BuildConfig
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties.getProperty("cloudinary_cloud_name")}\"")
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
    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Recycler View
    implementation(libs.androidx.recyclerview)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // Firebase Firestore Database
    implementation(libs.firebase.firestore)

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:25.0.1")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")

    // Dagger and ksp
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Glide Library For image
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.1.2")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp")

    // Work manager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Gson
    implementation("com.google.code.gson:gson:2.13.1")
}