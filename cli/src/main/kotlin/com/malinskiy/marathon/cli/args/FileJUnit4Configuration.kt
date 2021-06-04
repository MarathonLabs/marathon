package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import java.io.File

class FileJUnit4Configuration(
    @JsonProperty("applicationClasspath") val applicationClasspath: List<File>?,
    @JsonProperty("testClasspath") val testClasspath: List<File>?,
) : FileVendorConfiguration {

    fun toJUnit4Configuration(applicationClasspath: List<File>?, testClasspath: List<File>?): Junit4Configuration {
        return Junit4Configuration(
            applicationClasspath = mutableListOf<File>().apply {
                this@FileJUnit4Configuration.applicationClasspath?.let { addAll(it) }
                applicationClasspath?.let { addAll(it) }
            },
            testClasspath = mutableListOf<File>().apply {
                this@FileJUnit4Configuration.testClasspath?.let { addAll(it) }
                testClasspath?.let { addAll(it) }
            }
        )
    }
}
