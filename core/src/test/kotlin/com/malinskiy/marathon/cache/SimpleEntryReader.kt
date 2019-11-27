package com.malinskiy.marathon.cache

import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining
import kotlinx.io.core.readBytes
import java.nio.charset.StandardCharsets

class SimpleEntryReader : CacheEntryReader {

    var readInvoked: Boolean = false
    var bytes: ByteArray? = null
    val data: String?
        get() = bytes?.toString(charset = StandardCharsets.UTF_8)

    override suspend fun readFrom(input: ByteReadChannel) {
        readInvoked = true
        bytes = input.readRemaining().readBytes()
    }
}
