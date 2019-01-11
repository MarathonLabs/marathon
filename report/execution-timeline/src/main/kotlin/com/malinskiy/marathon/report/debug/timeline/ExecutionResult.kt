package com.malinskiy.marathon.report.debug.timeline

data class ExecutionResult(val passedTests: Int,
                           val failedTests: Int,
                           val ignoredTests: Int,
                           val executionStats: ExecutionStats,
                           val measures: List<Measure>)
