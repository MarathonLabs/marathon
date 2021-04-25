package com.malinskiy.marathon.lite.configuration

import java.io.Serializable

data class FileSyncConfiguration(
    val pull: List<FileSyncEntry> = listOf()
) : Serializable

data class FileSyncEntry(
    val relativePath: String,
    val aggregationMode: AggregationMode = AggregationMode.DEVICE
) : Serializable

enum class AggregationMode {
    DEVICE,
    POOL,
    DEVICE_AND_POOL,
    TEST_RUN
}
