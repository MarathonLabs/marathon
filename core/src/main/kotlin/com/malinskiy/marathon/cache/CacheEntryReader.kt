package com.malinskiy.marathon.cache

import java.io.InputStream

interface CacheEntryReader {
    fun readFrom(input: InputStream)
}
