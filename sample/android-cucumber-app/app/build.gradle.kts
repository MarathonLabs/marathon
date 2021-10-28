plugins {
    id("com.android.application")
    id("jacoco")
    id("kotlin-android")
}

android {
    buildToolsVersion("30.0.2")
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)

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
            isTestCoverageEnabled = true
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
