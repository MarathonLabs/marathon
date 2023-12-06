package com.malinskiy.marathon.report.raw

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.Reporter

class RawJsonReporter(
    private val fileManager: FileManager,
    private val gson: Gson
) : Reporter {

    override fun generate(executionReport: ExecutionReport) {
        val testResults = executionReport.testEvents.map {
            val metaPropertiesList = it.testResult.test.metaProperties.map { metaProp ->
                val valuesAsStringMap = metaProp.values.mapValues { (_, value) ->
                    value?.toString() ?: "" // Convert value to String, handling nulls
                }
                MetaPropertyJson(
                    name = metaProp.name,
                    values = valuesAsStringMap
                )
            }

            RawTestRun(
                it.testResult.test.pkg,
                it.testResult.test.clazz,
                it.testResult.test.method,
                metaPropertiesList,
                it.device.serialNumber,
                it.testResult.status,
                it.testResult.isIgnored,
                it.testResult.isSuccess,
                it.testResult.startTime,
                it.testResult.durationMillis()
            )
        }

        val outputFile = fileManager.createTestResultFile("raw.json")
        outputFile.writeText(gson.toJson(testResults))
    }

    data class RawTestRun(
        @SerializedName("package") val pkg: String,
        @SerializedName("class") val clazz: String,
        @SerializedName("method") val method: String,
        @SerializedName("metaProperties") val metaProperties: List<MetaPropertyJson>,
        @SerializedName("deviceSerial") val deviceSerial: String,
        @SerializedName("status") val status: TestStatus,
        @SerializedName("ignored") val ignored: Boolean,
        @SerializedName("success") val success: Boolean,
        @SerializedName("timestamp") val timestamp: Long,
        @SerializedName("duration") val duration: Long
    )

    data class MetaPropertyJson(
        @SerializedName("name") val name: String,
        @SerializedName("values") val values: Map<String, String> = emptyMap()
    )
}
