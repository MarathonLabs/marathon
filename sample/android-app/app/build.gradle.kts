plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("marathon") version "0.6.1-SNAPSHOT"
}

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(18)
        targetSdkVersion(29)

        applicationId = "com.example"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    androidTestImplementation(TestLibraries.testRunner)
    androidTestImplementation(TestLibraries.testRules)
    androidTestImplementation(TestLibraries.extJunit)
    androidTestImplementation(TestLibraries.espressoCore)
    androidTestImplementation(TestLibraries.kakao)
    androidTestImplementation(TestLibraries.allureKotlinCommon)
    androidTestImplementation(TestLibraries.allureKotlinModel)
    androidTestImplementation(TestLibraries.allureKotlinJunit4)
    androidTestImplementation(TestLibraries.allureKotlinAndroid)
}
