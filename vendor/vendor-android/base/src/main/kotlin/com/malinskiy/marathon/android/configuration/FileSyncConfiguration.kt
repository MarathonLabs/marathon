package com.malinskiy.marathon.android.configuration

data class FileSyncConfiguration(
    val pull: MutableList<FileSyncEntry> = mutableListOf()
)

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
