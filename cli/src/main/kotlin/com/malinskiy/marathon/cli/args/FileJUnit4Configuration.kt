package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.parsing.TestBundle
import java.io.File

class FileJUnit4Configuration(
    @JsonProperty("applicationClasspath") val applicationClasspath: List<File>?,
    @JsonProperty("testClasspath") val testClasspath: List<File>?,
    @JsonProperty("source") val source: File? = null,
) : FileVendorConfiguration {

    fun toJUnit4Configuration(mapper: ObjectMapper, applicationClasspath: List<File>?, testClasspath: List<File>?): Junit4Configuration {
        val testBundles = mutableListOf<TestBundle>()

        source?.let { folder ->
            for (file in folder.walkTopDown()) {
                if (file.isFile) {
                    val subconfig = mapper.readValue<FileJUnit4Configuration>(file.readText())
                    testBundles.add(
                        TestBundle(
                            file.name,
                            applicationClasspath = subconfig.applicationClasspath,
                            testClasspath = subconfig.testClasspath
                        )
                    )
                }
            }
        }

        return Junit4Configuration(
            applicationClasspath = mutableListOf<File>().apply {
                this@FileJUnit4Configuration.applicationClasspath?.let { addAll(it) }
                applicationClasspath?.let { addAll(it) }
            },
            testClasspath = mutableListOf<File>().apply {
                this@FileJUnit4Configuration.testClasspath?.let { addAll(it) }
                testClasspath?.let { addAll(it) }
            },
            testBundles = testBundles.toList(),
        )
    }
}
