package com.malinskiy.marathon.cli.schema.android

import java.io.Serializable

val DEFAULT_ALLURE_CONFIGURATION = AllureConfiguration(
    enabled = false
)

data class AllureConfiguration(
    var enabled: Boolean = false,
    var resultsDirectory: String = "/sdcard/allure-results"
) : Serializable
