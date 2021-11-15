package com.malinskiy.marathon.config.vendor.android

/**
 * @param relativeResultsDirectory path of allure results relative to app data folder: for /data/data/com.example/files/allure-results this will be "/files/allure-results"
 */
data class AllureConfiguration(
    var enabled: Boolean = false,
    var relativeResultsDirectory: String = "/files/allure-results"
)
