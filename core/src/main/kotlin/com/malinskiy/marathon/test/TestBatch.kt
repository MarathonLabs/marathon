package com.malinskiy.marathon.test

import com.malinskiy.marathon.execution.Configuration
import kotlin.math.min

data class TestBatch(val tests: List<Test>)

fun TestBatch.calculateTimeout(configuration: Configuration): Long =
        min(configuration.testOutputTimeoutMillis * tests.size, configuration.testBatchTimeoutMillis)
