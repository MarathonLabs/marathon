package com.malinskiy.marathon.cache

import kotlinx.coroutines.io.ByteWriteChannel

interface CacheEntryWriter {
    suspend fun writeTo(output: ByteWriteChannel)
    val size: Long
}
