plugins {
    id("com.android.library")
    id("kotlin-android")
    id("marathon") version "0.6.0-SNAPSHOT"
}

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(29)

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
    implementation(Libraries.appCompat)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.kotlinStdLib)
    implementation(TestLibraries.espressoRunner)
    implementation(TestLibraries.espressoCore)
}
