package com.malinskiy.marathon.cache

class NoOpCacheService : CacheService {

    override suspend fun load(key: CacheKey, reader: CacheEntryReader): Boolean {
        return false
    }

    override suspend fun store(key: CacheKey, writer: CacheEntryWriter) {
    }

    override fun close() {
    }
}
