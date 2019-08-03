package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.test.Test
import java.util.*

interface SortingStrategy {
    fun process(metricsProvider: MetricsProvider): Comparator<Test>
}
