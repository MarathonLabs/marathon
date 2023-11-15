import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration

plugins {
    id("com.android.test")
    id("kotlin-android")
    id("com.malinskiy.marathon") version "0.8.5-SNAPSHOT"
}

android {
    buildToolsVersion = "34.0.0"
    compileSdk = 33

    namespace = "com.example.ui_tests"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath(":app")
}

dependencies {
    implementation(TestLibraries.testRunner)
    implementation(TestLibraries.extJunit)
    implementation(TestLibraries.espressoCore)
}

marathon {
    testParserConfiguration = TestParserConfiguration.RemoteTestParserConfiguration()
    uncompletedTestRetryQuota = 3
}
