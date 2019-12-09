package com.malinskiy.marathon.execution

import com.malinskiy.marathon.cache.config.LocalCacheConfiguration
import com.malinskiy.marathon.cache.config.RemoteCacheConfiguration

// TODO: support optional cache push
data class CacheConfiguration(
    val local: LocalCacheConfiguration = LocalCacheConfiguration.Disabled,
    val remote: RemoteCacheConfiguration = RemoteCacheConfiguration.Disabled
) {

    val isEnabled: Boolean
        get() = local !is LocalCacheConfiguration.Disabled || remote !is RemoteCacheConfiguration.Disabled

}
