package com.malinskiy.marathon

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.test.Test
import java.time.Instant

class MetricsProviderStub(val successRate: Double = 0.5,
                          val executionTime: Double = 1500.0) : MetricsProvider {
    override fun successRate(test: Test, limit: Instant): Double {
        return successRate
    }

    override fun executionTime(test: Test, percentile: Double, limit: Instant): Double {
        return executionTime
    }
}
