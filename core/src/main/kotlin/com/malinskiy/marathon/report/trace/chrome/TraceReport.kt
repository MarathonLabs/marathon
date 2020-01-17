package com.malinskiy.marathon.report.trace.chrome

import com.google.gson.annotations.SerializedName

data class TraceReport(
    @SerializedName("traceEvents") val traceEvents: List<TraceEvent>,
    @SerializedName("otherData") val metadata: Map<String, String>? = null
)