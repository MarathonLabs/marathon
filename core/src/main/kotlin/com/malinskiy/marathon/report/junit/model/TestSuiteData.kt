package com.malinskiy.marathon.report.junit.model

data class TestSuiteData(
    val name: String,
    val tests: Int,
    val failures: Int,
    val errors: Int,
    val skipped: Int,
    val time: String,
    val timeStamp: String
)
