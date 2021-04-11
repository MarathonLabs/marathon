package com.malinskiy.marathon.lite.configuration.android

import java.io.Serializable

data class FileSyncConfiguration (
    val pull: MutableList<FileSyncEntry> = mutableListOf()
): Serializable

data class FileSyncEntry(
    val relativePath: String,
    val aggregationMode: AggregationMode = AggregationMode.DEVICE
)

enum class AggregationMode {
    DEVICE,
    POOL,
    DEVICE_AND_POOL,
    TEST_RUN
}
