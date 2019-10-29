package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.testrunner.TestResult
import com.malinskiy.marathon.execution.TestStatus

fun TestResult.TestStatus.toMarathonStatus() = when (this) {
    TestResult.TestStatus.PASSED -> TestStatus.PASSED
    TestResult.TestStatus.FAILURE -> TestStatus.FAILURE
    TestResult.TestStatus.IGNORED -> TestStatus.IGNORED
    TestResult.TestStatus.INCOMPLETE -> TestStatus.INCOMPLETE
    TestResult.TestStatus.ASSUMPTION_FAILURE -> TestStatus.ASSUMPTION_FAILURE
}