package com.malinskiy.marathon.execution.result

import com.malinskiy.marathon.execution.TestStatus

data class TemporalTestResult(
    var status: TestStatus = TestStatus.INCOMPLETE,
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long = 0,
    var stackTrace: String? = null,
    var metrics: Map<String, String>? = null
) {
    fun isSuccessful(): Boolean =
        when (status) {
            TestStatus.PASSED, TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> true
            else -> false
        }
}
