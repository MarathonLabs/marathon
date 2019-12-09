package com.malinskiy.marathon.cache

import com.malinskiy.marathon.cache.config.LocalCacheConfiguration
import com.malinskiy.marathon.cache.config.RemoteCacheConfiguration
import com.malinskiy.marathon.cache.gradle.GradleHttpCacheService
import com.malinskiy.marathon.execution.Configuration
import java.lang.IllegalArgumentException

class CacheServiceFactory(private val configuration: Configuration) {

    fun createCacheService(): CacheService {
        if (configuration.cache.local !is LocalCacheConfiguration.Disabled) throw IllegalArgumentException("Local cache is not supported yet")

        return when (configuration.cache.remote) {
            is RemoteCacheConfiguration.Enabled -> GradleHttpCacheService(configuration.cache.remote)
            is RemoteCacheConfiguration.Disabled -> NoOpCacheService()
        }
    }

}
