import com.malinskiy.marathon.config.vendor.android.TestAccessConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("marathon") version "0.7.3-SNAPSHOT"
}

android {
    buildToolsVersion = "30.0.3"
    compileSdk = 30

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/io.netty.versions.properties")
    }

    defaultConfig {
        minSdk = 21
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
            isTestCoverageEnabled = false
        }
    }
}

marathon {
    allureConfiguration {
        enabled = true
    }
    applicationPmClear = true
    testApplicationPmClear = true
    autoGrantPermission = true
    isCodeCoverageEnabled = true
    testParserConfiguration = TestParserConfiguration.RemoteTestParserConfiguration(
        mapOf("listener" to "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer")
    )
    uncompletedTestRetryQuota = 3
    testAccessConfiguration = TestAccessConfiguration(adb = true, grpc = true, console = true)
    fileSyncConfiguration {
        allureConfiguration {
            enabled = true
            relativeResultsDirectory = "files/allure-results"
        }
    }
}

dependencies {
    implementation(Libraries.appCompat)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.kotlinStdLib)

    androidTestImplementation(TestLibraries.testOutputEnhancer)
    androidTestImplementation(TestLibraries.adamJunit4)
    androidTestImplementation(TestLibraries.testRunner)
    androidTestImplementation(TestLibraries.testRules)
    androidTestImplementation(TestLibraries.extJunit)
    androidTestImplementation(TestLibraries.espressoCore)
    androidTestImplementation(TestLibraries.kakao)
    androidTestImplementation(TestLibraries.allureKotlinCommon)
    androidTestImplementation(TestLibraries.allureKotlinModel)
    androidTestImplementation(TestLibraries.allureKotlinJunit4)
    androidTestImplementation(TestLibraries.allureKotlinAndroid)
    androidTestImplementation(TestLibraries.adamScreencapture)
}
