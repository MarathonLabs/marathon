package com.malinskiy.marathon.cache

import kotlinx.coroutines.io.ByteChannel
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.io.readRemaining
import kotlinx.io.core.readBytes
import java.lang.RuntimeException

class MemoryCacheService : CacheService {

    private var throwable: Throwable? = null
    private val cache = hashMapOf<CacheKey, ByteArray>()

    override suspend fun load(key: CacheKey, reader: CacheEntryReader): Boolean {
        throwable?.let { throw it }

        val value = cache[key] ?: return false
        reader.readFrom(ByteReadChannel(value))
        return true
    }

    override suspend fun store(key: CacheKey, writer: CacheEntryWriter) {
        throwable?.let { throw it }

        val channel = ByteChannel()
        writer.writeTo(channel)
        channel.close()
        cache[key] = channel.readRemaining().readBytes()
    }

    override fun close() {
    }

    fun throwExceptions(throwable: Throwable = RuntimeException("Test exception")) {
        this.throwable = throwable
    }
}
