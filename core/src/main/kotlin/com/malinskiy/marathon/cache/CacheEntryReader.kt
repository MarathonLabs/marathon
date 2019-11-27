package com.malinskiy.marathon.cache

import kotlinx.coroutines.io.ByteReadChannel

interface CacheEntryReader {
    suspend fun readFrom(input: ByteReadChannel)
}
