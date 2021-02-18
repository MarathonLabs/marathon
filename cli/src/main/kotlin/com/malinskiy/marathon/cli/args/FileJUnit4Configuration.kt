package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import java.io.File

class FileJUnit4Configuration(
    @JsonProperty("applicationJar") val applicationJar: File,
    @JsonProperty("testsJar") val testsJar: File,
) : FileVendorConfiguration {

    fun toJUnit4Configuration(): Junit4Configuration {
        return Junit4Configuration(
            applicationJar = applicationJar,
            testsJar = testsJar
        )
    }
}
