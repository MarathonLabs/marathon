package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.Test

interface RetryStrategy {
//    var totalAllowedRetryQuota: Int?
//    var retryPerTestCaseQuota: Int?
//
//    fun shouldRetry(test: Test)
//    fun deviceSelector()
    fun process(tests: Collection<Test>, testShard: TestShard) : List<Test>
}
