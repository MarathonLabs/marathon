package com.malinskiy.marathon.config.strategy

import java.time.Instant

sealed class BatchingStrategyConfiguration {
    data class FixedSizeBatchingStrategyConfiguration(
        val size: Int,
        val durationMillis: Long? = null,
        val percentile: Double? = null,
        val timeLimit: Instant? = null,
        val lastMileLength: Int = 0
    ) : BatchingStrategyConfiguration()

    object IsolateBatchingStrategyConfiguration : BatchingStrategyConfiguration() 
}
