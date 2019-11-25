package com.malinskiy.marathon.cache

interface CacheService {
    suspend fun load(key: CacheKey, reader: CacheEntryReader): Boolean
    suspend fun store(key: CacheKey, writer: CacheEntryWriter)
    fun close()
}
