package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.test.Test

internal class NoOpMetricsProvider : MetricsProvider {
    override fun successRate(test: Test): Double {
        return 0.0
    }

    override fun executionTime(test: Test): Double {
        return 0.0
    }
}
