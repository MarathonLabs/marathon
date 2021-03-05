package com.malinskiy.marathon.android.configuration

val DEFAULT_ALLURE_CONFIGURATION = AllureConfiguration(
    enabled = false
)

/**
 * @param relativeResultsDirectory path of allure results relative to $EXTERNAL_STORAGE (Environment.getExternalStorageDirectory())
 */
data class AllureConfiguration(
    var enabled: Boolean = false,
    var relativeResultsDirectory: String = "/allure-results"
)
