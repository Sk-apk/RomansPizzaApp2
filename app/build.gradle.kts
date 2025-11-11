import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("com.google.gms.google-services")



}

android {
    namespace = "com.example.romanspizza"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.romanspizza"
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


    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Security for password hashing
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

            // Firebase BOM (Bill of Materials)
    implementation (platform("com.google.firebase:firebase-bom:32.7.0"))

            // Firebase services
            implementation ("com.google.firebase:firebase-auth-ktx")
            implementation ("com.google.firebase:firebase-firestore-ktx")
            implementation ("com.google.firebase:firebase-storage-ktx")

            // Coroutines for async operations
            implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")


}

