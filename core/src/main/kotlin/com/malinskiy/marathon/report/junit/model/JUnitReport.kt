package com.malinskiy.marathon.report.junit.model

data class JUnitReport(
    val testSuiteData: TestSuiteData,
    val testCases: List<TestCaseData>
)
