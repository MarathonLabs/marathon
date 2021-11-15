package com.malinskiy.marathon.config.vendor.android

data class FileSyncConfiguration(
    val pull: MutableSet<FileSyncEntry> = mutableSetOf()
)

data class FileSyncEntry(
    val relativePath: String,
    val pathRoot: PathRoot = PathRoot.EXTERNAL_STORAGE,
    val aggregationMode: AggregationMode = AggregationMode.DEVICE
)

/**
 * @property EXTERNAL_STORAGE Path is relative to external storage mount, e.g. /sdcard/some-folder
 * @property APP_DATA Path is relative to app data folder (/data/data/$PKG/), e.g. /data/data/com.example/files/somefolder
 */
enum class PathRoot {
    EXTERNAL_STORAGE,
    APP_DATA,
}

enum class AggregationMode {
    DEVICE,
    POOL,
    DEVICE_AND_POOL,
    TEST_RUN
}
