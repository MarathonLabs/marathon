package com.malinskiy.marathon

import com.malinskiy.marathon.cache.config.Credentials
import com.malinskiy.marathon.cache.config.LocalCacheConfiguration
import com.malinskiy.marathon.cache.config.RemoteCacheConfiguration
import com.malinskiy.marathon.execution.CacheConfiguration
import groovy.lang.Closure
import java.io.File
import java.lang.IllegalArgumentException

open class CachePluginConfiguration {

    var localExtension: LocalCacheExtension? = null
    var remoteExtension: RemoteCacheExtension? = null

    fun local(closure: Closure<*>) {
        localExtension = LocalCacheExtension()
        closure.delegate = localExtension
        closure.call()
    }

    fun remote(closure: Closure<*>) {
        remoteExtension = RemoteCacheExtension()
        closure.delegate = remoteExtension
        closure.call()
    }

    fun local(block: LocalCacheExtension.() -> Unit) {
        val config = localExtension ?: LocalCacheExtension()
        config.also(block)
        localExtension = config
    }

    fun remote(block: RemoteCacheExtension.() -> Unit) {
        val config = remoteExtension ?: RemoteCacheExtension()
        config.also(block)
        remoteExtension = config
    }
}

private val DEFAULT_LOCAL_CACHE_DIRECTORY = File("~/cache/marathon")
private const val DEFAULT_LOCAL_UNUSED_ENTRIES_DELETE_AFTER_DAYS = 10

open class LocalCacheExtension {
    var directory: File = DEFAULT_LOCAL_CACHE_DIRECTORY
    var removeUnusedEntriesAfterDays: Int = DEFAULT_LOCAL_UNUSED_ENTRIES_DELETE_AFTER_DAYS
}

private fun LocalCacheExtension?.toConfig(): LocalCacheConfiguration =
    this?.let {
        LocalCacheConfiguration.Enabled(it.directory, it.removeUnusedEntriesAfterDays)
    } ?: LocalCacheConfiguration.Disabled

open class RemoteCacheExtension {
    var url: String? = null
    var credentials: Credentials? = null
}

private fun RemoteCacheExtension?.toConfig(): RemoteCacheConfiguration =
    this?.let {
        val url = it.url ?: throw IllegalArgumentException("Remote cache URL is required for remote cache configuration")
        RemoteCacheConfiguration.Enabled(url, it.credentials)
    } ?: RemoteCacheConfiguration.Disabled


fun CachePluginConfiguration.toCacheConfiguration(): CacheConfiguration {
    return CacheConfiguration(
        local = localExtension.toConfig(),
        remote = remoteExtension.toConfig()
    )
}
