package com.malinskiy.marathon.execution.listener

import com.malinskiy.marathon.test.Test

/**
 * General lifecycle of any test batch execution
 */
interface TestRunListener {
    suspend fun beforeTestRun() {}
    suspend fun testStarted(test: Test)
    /**
     * Called regardless of the test passing/failing unless test run is interrupted
     */
    suspend fun testEnded(test: Test)
    suspend fun afterTestRun() {}
}
