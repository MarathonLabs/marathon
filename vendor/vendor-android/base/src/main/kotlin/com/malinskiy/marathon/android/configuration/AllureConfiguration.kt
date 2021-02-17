package com.malinskiy.marathon.android.configuration

val DEFAULT_ALLURE_CONFIGURATION = AllureConfiguration(
    enabled = false
)

data class AllureConfiguration(
    var enabled: Boolean = false,
    var resultsDirectory: String = "/sdcard/allure-results"
)
