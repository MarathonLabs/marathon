package com.malinskiy.marathon.android

import com.android.ddmlib.testrunner.TestIdentifier
import com.android.ddmlib.testrunner.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test


fun TestResult.TestStatus.toMarathonStatus() = when (this) {
    TestResult.TestStatus.PASSED -> TestStatus.PASSED
    TestResult.TestStatus.FAILURE -> TestStatus.FAILURE
    TestResult.TestStatus.IGNORED -> TestStatus.IGNORED
    TestResult.TestStatus.INCOMPLETE -> TestStatus.INCOMPLETE
    TestResult.TestStatus.ASSUMPTION_FAILURE -> TestStatus.ASSUMPTION_FAILURE
}

fun TestIdentifier.toTest(): Test {
    val pkg = className.substringBeforeLast(".")
    val className = className.substringAfterLast(".")
    val methodName = testName
    return Test(pkg, className, methodName, emptyList())
}
