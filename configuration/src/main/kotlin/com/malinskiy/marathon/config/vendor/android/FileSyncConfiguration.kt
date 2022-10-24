package com.malinskiy.marathon.config.vendor.android

import java.io.Serializable

data class FileSyncConfiguration(
    val pull: MutableSet<FileSyncEntry> = mutableSetOf(),
    val push: MutableSet<FilePushEntry> = mutableSetOf(),
) : Serializable

data class FileSyncEntry(
    val relativePath: String,
    val pathRoot: PathRoot = PathRoot.EXTERNAL_STORAGE,
    val aggregationMode: AggregationMode = AggregationMode.DEVICE
) : Serializable

data class FilePushEntry(
    val path: String,
    val pathRoot: PathRoot = PathRoot.LOCAL_TMP,
) : Serializable

/**
 * @property EXTERNAL_STORAGE Path is relative to external storage mount, e.g. /sdcard/some-folder
 * @property APP_DATA Path is relative to app data folder (/data/data/$PKG/), e.g. /data/data/com.example/files/somefolder
 * @property LOCAL_TMP Path is relative to device tmp folder (/data/local/tmp)
 */
enum class PathRoot {
    EXTERNAL_STORAGE,
    APP_DATA,
    LOCAL_TMP
}

enum class AggregationMode {
    DEVICE,
    POOL,
    DEVICE_AND_POOL,
    TEST_RUN
}
