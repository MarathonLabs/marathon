package com.malinskiy.marathon.report.timeline

data class TimelineExecutionResult(
    val passedTests: Int,
    val failedTests: Int,
    val ignoredTests: Int,
    val executionStats: ExecutionStats,
    val measures: List<Measure>
)
