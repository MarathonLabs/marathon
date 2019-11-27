package com.malinskiy.marathon.cache

import kotlinx.coroutines.io.ByteChannel
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining
import kotlinx.io.core.readBytes

class MemoryCacheService : CacheService {

    private val cache = hashMapOf<CacheKey, ByteArray>()

    override suspend fun load(key: CacheKey, reader: CacheEntryReader): Boolean {
        val value = cache[key] ?: return false
        reader.readFrom(ByteReadChannel(value))
        return true
    }

    override suspend fun store(key: CacheKey, writer: CacheEntryWriter) {
        val channel = ByteChannel()
        writer.writeTo(channel)
        cache[key] = channel.readRemaining().readBytes()
    }

    override fun close() {
    }
}
