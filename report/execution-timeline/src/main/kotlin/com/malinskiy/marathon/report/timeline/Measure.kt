package com.malinskiy.marathon.report.timeline

import com.google.gson.annotations.SerializedName


data class Measure(
    @SerializedName("measure") val measure: String,
    @SerializedName("stats") val executionStats: ExecutionStats,
    @SerializedName("data") val data: List<Data>,
    /** Populated in schema 2+; null when the reporter runs without device metadata. */
    @SerializedName("device") val device: TimelineDevice? = null,
)

data class TimelineDevice(
    @SerializedName("serial") val serial: String,
    @SerializedName("modelName") val modelName: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("osVersion") val osVersion: String,
    @SerializedName("osMajor") val osMajor: Int?,
)
