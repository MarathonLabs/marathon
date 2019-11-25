package com.malinskiy.marathon.cache.config

import java.io.File

sealed class LocalCacheConfiguration {

    data class Enabled(
        val directory: File,
        val removeUnusedEntriesAfterDays: Int
    ) : LocalCacheConfiguration()

    object Disabled : LocalCacheConfiguration()

}
