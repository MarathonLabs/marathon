package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.test.Test

interface Tracker {
    fun trackTestStarted(test: Test, time: Int)
    fun trackTestFinished(test: Test, success: Boolean, time: Int)
    fun trackTestIgnored(test: Test)

    fun calculateTestExpectedTime(test: Test) : Int
    fun calculateTestVariance(test: Test) : Int
    fun calculateTestExpectedRetries(test: Test) : Int
}
