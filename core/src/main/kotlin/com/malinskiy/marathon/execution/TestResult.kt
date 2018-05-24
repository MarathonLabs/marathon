package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

data class TestResult(val test: Test,
                      val status: TestStatus,
                      val startTime: Long,
                      val endTime: Long,
                      val stacktrace: String? = null)
