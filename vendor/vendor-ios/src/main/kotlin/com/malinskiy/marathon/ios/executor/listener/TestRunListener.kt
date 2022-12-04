package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.test.Test

interface TestRunListener {
    fun testStarted(test: Test)
    fun testFailed(test: Test, startTime: Long, endTime: Long)
    fun testPassed(test: Test, startTime: Long, endTime: Long)
    fun batchFinished()
}
