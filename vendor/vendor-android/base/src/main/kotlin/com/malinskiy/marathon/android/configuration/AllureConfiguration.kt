package com.malinskiy.marathon.android.configuration

val DEFAULT_ALLURE_CONFIGURATION = AllureConfiguration(
    allureAndroidSupport = false
)

data class AllureConfiguration(
    val allureAndroidSupport: Boolean,
    val resultsDirectory: String = "/sdcard/allure-results"
)
