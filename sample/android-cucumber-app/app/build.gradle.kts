plugins {
    id("com.android.application")
    id("jacoco")
    id("kotlin-android")
}

android {
    buildToolsVersion = "30.0.3"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        applicationId = "cucumber.cukeulator"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "cucumber.cukeulator.test.CukeulatorAndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
        getByName("debug") {
            isTestCoverageEnabled = false
        }
    }
}

dependencies {
    implementation(Libraries.appCompat)
    implementation(Libraries.kotlinStdLib)

    androidTestImplementation(TestLibraries.cucumber)
    androidTestImplementation(TestLibraries.cucumberPicocontainer)
    androidTestImplementation(TestLibraries.testRules)
    androidTestImplementation(TestLibraries.espressoCore)
}
