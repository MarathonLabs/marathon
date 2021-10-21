plugins {
    id("com.android.application")
    id("kotlin-android")
    id("marathon") version "0.7.0-SNAPSHOT"
}

android {
    buildToolsVersion = "30.0.3"
    compileSdk = 30

    defaultConfig {
        minSdk = 18
        targetSdk = 30

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
        getByName("debug") {
            isTestCoverageEnabled = true
        }
    }
}

marathon {
    instrumentationArgs {
        put("debug", "false")
    }
    vendor = com.malinskiy.marathon.config.vendor.VendorConfiguration.AndroidConfiguration.VendorType.ADAM
    allureConfiguration {
        enabled = true
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
