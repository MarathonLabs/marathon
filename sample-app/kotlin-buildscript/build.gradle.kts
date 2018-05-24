plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("marathon") version "0.1.0"
}

android {
    buildToolsVersion("27.0.3")
    compileSdkVersion(27)

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(27)

        applicationId = "com.example"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("com.android.support:appcompat-v7:27.0.1")
    implementation("com.android.support.constraint:constraint-layout:1.0.2")
    implementation(TestLibraries.espressoRunner)
    implementation(TestLibraries.espressoCore)
    implementation(kotlin("stdlib", "1.2.0"))
    androidTestImplementation(TestLibraries.kakao)
}
