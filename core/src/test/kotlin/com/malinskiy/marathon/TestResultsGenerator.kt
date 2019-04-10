package com.malinskiy.marathon

import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test

fun generateTestResult() : TestResult = generateTestResult(generateTest())

fun generateTestResult(test: Test): TestResult = TestResult(
        test,
        createDeviceInfo(),
        TestStatus.PASSED,
        0,
        10000
)

fun generateTestResults(tests: List<Test>): List<TestResult> {
    return tests.map {
        generateTestResult(it)
    }
}