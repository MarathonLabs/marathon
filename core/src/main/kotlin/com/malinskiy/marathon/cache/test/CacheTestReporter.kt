package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter

/**
 * Reports progress for tests loaded from cache
 */
class CacheTestReporter(
    private val progressReporter: ProgressReporter,
    private val track: Track
) {

    fun onCachedTest(poolId: DevicePoolId, testResult: TestResult) {
        progressReporter.addTests(poolId, 1)
        track.test(poolId, testResult.device, testResult, final = true)

        when (testResult.status) {
            TestStatus.FAILURE, TestStatus.INCOMPLETE -> progressReporter.testFailed(poolId, testResult.device, testResult.test)
            TestStatus.PASSED -> progressReporter.testPassed(poolId, testResult.device, testResult.test)
            TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> progressReporter.testIgnored(poolId, testResult.device, testResult.test)
        }
    }

}
