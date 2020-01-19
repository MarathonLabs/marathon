package com.malinskiy.marathon.report.junit.model

import com.malinskiy.marathon.report.junit.TestCaseData
import com.malinskiy.marathon.report.junit.TestSuiteData

data class JUnitReport(
    val testSuiteData: TestSuiteData,
    val testCases: List<TestCaseData>
)
