package com.malinskiy.marathon.config.vendor.android

/**
 * @param relativeResultsDirectory path of allure results relative to $EXTERNAL_STORAGE (Environment.getExternalStorageDirectory())
 */
data class AllureConfiguration(
    var enabled: Boolean = false,
    var relativeResultsDirectory: String = "/allure-results"
)
