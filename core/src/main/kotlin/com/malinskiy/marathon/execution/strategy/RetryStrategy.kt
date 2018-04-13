package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.test.Test

interface RetryStrategy {
    fun shouldRetry(test: Test)
    fun deviceSelector()
}