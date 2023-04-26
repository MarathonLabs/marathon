plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.malinskiy.marathon") version "0.8.2-SNAPSHOT"
}

android {
    buildToolsVersion = "30.0.3"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(TestLibraries.testRunner)
    implementation(TestLibraries.extJunit)
}
