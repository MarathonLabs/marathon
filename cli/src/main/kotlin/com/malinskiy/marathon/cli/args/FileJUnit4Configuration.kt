package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.model.JUnit4TestBundle
import java.io.File


class FileJUnit4Configuration(
    @JsonProperty("applicationClasspath") val applicationClasspath: List<File>?,
    @JsonProperty("testClasspath") val testClasspath: List<File>?,
    @JsonProperty("testPackageRoot") val testPackageRoot: String,
    @JsonProperty("source") val source: File? = null,
    @JsonProperty("debugBooter") val debugBooter: Boolean = false,
    @JsonProperty("parallelism") val parallelism: Int? = null,
    @JsonProperty("forkEvery") val forkEvery: Int = 1000,
    @JsonProperty("javaHome") val javaHome: File? = null,
    @JsonProperty("javaOptions") val javaOptions: List<String> = emptyList(),
    @JsonProperty("useArgfiles") val useArgfiles: Boolean = true,

    ) : FileVendorConfiguration {

    fun toJUnit4Configuration(
        mapper: ObjectMapper,
        applicationClasspath: List<File>?,
        testClasspath: List<File>?,
        javaHome: File?
    ): Junit4Configuration {
        val testBundles = mutableListOf<JUnit4TestBundle>()

        source?.let { folder ->
            for (file in folder.walkTopDown()) {
                if (file.isFile) {
                    val subconfig = mapper.readValue<FileJUnit4ModuleConfiguration>(file.readText())
                    testBundles.add(
                        JUnit4TestBundle(
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
            testPackageRoot = testPackageRoot,
            testBundles = testBundles.toList(),
            debugBooter = debugBooter,
            parallelism = parallelism,
            forkEvery = forkEvery,
            javaHome = this.javaHome ?: javaHome,
            javaOptions = javaOptions,
            useArgfiles = useArgfiles,
        )
    }
}
