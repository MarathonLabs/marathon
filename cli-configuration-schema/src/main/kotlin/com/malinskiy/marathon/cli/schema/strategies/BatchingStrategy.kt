package com.malinskiy.marathon.cli.schema.strategies

import java.time.Instant

sealed class BatchingStrategy {
    data class FixedSize(
        val size: Int,
        val durationMillis: Long? = null,
        val percentile: Double? = null,
        val timeLimit: Instant? = null,
        val lastMileLength: Int = 0
    ) : BatchingStrategy()

    object Disabled : BatchingStrategy()
}
