package com.malinskiy.marathon.report.timeline

import com.google.gson.annotations.SerializedName


data class Measure(@SerializedName("measure") val measure: String,
                   @SerializedName("stats") val executionStats: ExecutionStats,
                   @SerializedName("data") val data: List<Data>)
