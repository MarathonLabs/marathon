plugins {
    id("com.android.library")
    id("kotlin-android")
    id("marathon") version "0.7.0-SNAPSHOT"
}

android {
    buildToolsVersion = "30.0.3"
    compileSdk = 30

    defaultConfig {
        minSdk = 16
        targetSdk = 30
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
