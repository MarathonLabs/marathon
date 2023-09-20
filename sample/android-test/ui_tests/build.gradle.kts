@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("com.android.test")
//    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.example.ui_tests"
    compileSdk = 34
    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = null
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
//    targetProjectPath(":app_api")
    targetProjectPath(":app_horizont_api")
}

dependencies {

    implementation(libs.junit)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.espresso.core)
    implementation(project(":app_impl"))
    implementation(project(":app_horizont_impl"))
}