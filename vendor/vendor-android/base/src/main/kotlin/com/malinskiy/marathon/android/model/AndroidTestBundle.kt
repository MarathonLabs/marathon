package com.malinskiy.marathon.android.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.execution.bundle.TestBundle
import java.io.File

class AndroidTestBundle(
    @JsonProperty("application") val application: File?,
    @JsonProperty("testApplication") val testApplication: File,
    @JsonProperty("extraApplications") val extraApplications: List<File>?,
    @JsonProperty("splitApks") val splitApks: List<File>?
) : TestBundle() {
    override val id: String
        get() = testApplication.absolutePath

    val instrumentationInfo: InstrumentationInfo by lazy { apkParser.parseInstrumentationInfo(testApplication) }

    companion object {
        val apkParser = ApkParser()
    }
}
