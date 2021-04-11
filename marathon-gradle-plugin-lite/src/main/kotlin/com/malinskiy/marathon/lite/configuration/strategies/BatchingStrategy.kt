package com.malinskiy.marathon.lite.configuration.strategies

import java.io.Serializable
import java.time.Instant

sealed class BatchingStrategy : Serializable {
    data class FixedSize(
        val fixedSize: Int,
        val durationMillis: Long? = null,
        val percentile: Double? = null,
        val timeLimit: Instant? = null,
        val lastMileLength: Int = 0
    ) : BatchingStrategy()

    object Disabled : BatchingStrategy()
}
