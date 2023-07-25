import com.malinskiy.marathon.config.vendor.android.TestAccessConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.malinskiy.marathon") version "0.8.2-SNAPSHOT"
}

android {
    buildToolsVersion = "33.0.0"
    compileSdk = 33

    namespace = "com.example"
    testNamespace = "com.example.test"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/io.netty.versions.properties")
    }

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        applicationId = "com.example"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        getByName("debug") {
            isMinifyEnabled = false
            enableAndroidTestCoverage = true
        }
    }
}

marathon {
    allureConfiguration {
        enabled = true
    }
    applicationPmClear = true
    disableWindowAnimation = false
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

configurations {
    forEach { configuration ->
        //Because Google is using a library from 2016 for proto and can't update it
        //https://github.com/google/Accessibility-Test-Framework-for-Android/issues/38
        configuration.exclude(group = "com.google.protobuf", module = "protobuf-lite")
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
