package com.malinskiy.marathon.config.vendor.android

/**
 * @param relativeResultsDirectory path of allure results relative to app data folder:
 * for /data/data/com.example/files/allure-results this will be "/files/allure-results"
 *
 * @param pathRoot path of base directory where allure results are written to.
 * Since allure-kotlin 2.3.0 this is the internal data directory of the app, e.g. /data/data/com.example/. This will be automatically
 * detected
 * Before allure-kotlin 2.3.0 the base directory was PathRoot.EXTERNAL_STORAGE.
 *
 */
data class AllureConfiguration(
    var enabled: Boolean = false,
    var relativeResultsDirectory: String = "/files/allure-results",
    val pathRoot: PathRoot = PathRoot.APP_DATA,
)
