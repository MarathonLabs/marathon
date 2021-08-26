package com.malinskiy.marathon.vendor.junit4.model

data class JUnit4TestResult(
    var status: JUnit4TestStatus = JUnit4TestStatus.INCOMPLETE,
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long = 0,
    var stackTrace: String? = null,
    var metrics: Map<String, String>? = null
)
