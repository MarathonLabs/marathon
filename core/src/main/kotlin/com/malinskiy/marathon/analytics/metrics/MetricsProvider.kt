package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.test.Test

interface MetricsProvider {
    fun successRate(test: Test): Double
    fun executionTime(test: Test): Double
}
