package com.malinskiy.marathon.report.timeline

import com.google.gson.annotations.SerializedName

data class TimelineExecutionResult(
    @SerializedName("reportSchemaVersion") val reportSchemaVersion: Int = REPORT_SCHEMA_VERSION,
    @SerializedName("passedTests") val passedTests: Int,
    @SerializedName("failedTests") val failedTests: Int,
    @SerializedName("ignoredTests") val ignoredTests: Int,
    @SerializedName("executionStats") val executionStats: ExecutionStats,
    @SerializedName("measures") val measures: List<Measure>,
) {
    companion object {
        /** V1: legacy shape (no device metadata, no schema version). V2: adds device details + batch/attempt on Data. */
        const val REPORT_SCHEMA_VERSION = 2
    }
}
