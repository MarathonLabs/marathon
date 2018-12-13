package com.malinskiy.marathon.android

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import com.android.ddmlib.testrunner.TestResult.TestStatus as DdmLibTestStatus

fun DdmLibTestStatus.toMarathonStatus() = when (this) {
    DdmLibTestStatus.PASSED -> TestStatus.PASSED
    DdmLibTestStatus.FAILURE -> TestStatus.FAILURE
    DdmLibTestStatus.IGNORED -> TestStatus.IGNORED
    DdmLibTestStatus.INCOMPLETE -> TestStatus.INCOMPLETE
    DdmLibTestStatus.ASSUMPTION_FAILURE -> TestStatus.ASSUMPTION_FAILURE
}

fun TestIdentifier.toTest(): Test {
    val pkg = className.substringBeforeLast(".")
    val className = className.substringAfterLast(".")
    val methodName = testName
    return Test(pkg, className, methodName, emptyList())
}
