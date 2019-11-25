package com.malinskiy.marathon.cache

import java.io.OutputStream

interface CacheEntryWriter {
    fun writeTo(output: OutputStream)
    val size: Long
}
