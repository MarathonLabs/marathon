package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration
import java.io.Serializable

data class AllureConfiguration(
    var enabled: Boolean = false,
    var resultsDirectory: String = "/sdcard/allure-results"
) : Serializable

fun AllureConfiguration.toProto(): AndroidConfiguration.AllureConfiguration {
    val builder = AndroidConfiguration.AllureConfiguration.newBuilder()
    builder.enabled = enabled
    builder.resultsDirectory = resultsDirectory
    return builder.build()
}
