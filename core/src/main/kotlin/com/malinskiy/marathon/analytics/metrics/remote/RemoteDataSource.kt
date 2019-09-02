package com.malinskiy.marathon.analytics.metrics.remote

import java.time.Instant

data class ExecutionTime(
    val testName: String,
    var percentile: Double
)

data class SuccessRate(
    val testName: String,
    val mean: Double
)

interface RemoteDataSource {
    fun requestAllSuccessRates(limit: Instant): List<SuccessRate>
    fun requestAllExecutionTimes(percentile: Double, limit: Instant): List<ExecutionTime>
    fun close()
}