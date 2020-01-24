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

        /**
         * It's fine to use the regular test runner, but this sample will also demo the integration with allure-android project
         * https://github.com/allure-framework/allure-android
         */
        testInstrumentationRunner = "io.qameta.allure.espresso.AllureAndroidRunner"
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

    androidTestImplementation(TestLibraries.espressoRunner)
    androidTestImplementation(TestLibraries.espressoRules)
    androidTestImplementation(TestLibraries.espressoCore)
    androidTestImplementation(TestLibraries.kakao)
    androidTestImplementation(TestLibraries.allureAndroidCommon)
    androidTestImplementation(TestLibraries.allureAndroidModel)
    androidTestImplementation(TestLibraries.allureAndroidEspresso)
}
