plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("marathon") version "0.5.2-SNAPSHOT"
}

android {
    buildToolsVersion("28.0.3")
    compileSdkVersion(28)

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

marathon {
    instrumentationArgs {
        put("debug", "false")
    }
}

dependencies {
    implementation(Libraries.appCompat)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.kotlinStdLib)
    implementation(TestLibraries.espressoRunner)
    implementation(TestLibraries.espressoCore)
    androidTestImplementation(TestLibraries.kakao)
}
