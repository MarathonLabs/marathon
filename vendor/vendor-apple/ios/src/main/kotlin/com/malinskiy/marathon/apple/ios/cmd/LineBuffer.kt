package com.malinskiy.marathon.apple.ios.cmd

import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.nio.charset.Charset

class LineBuffer(private val charset: Charset, private val onLine: suspend (String) -> Unit) : Closeable {
    private val stringBuffer = StringBuffer(16384)
    private val lock = Object()

    fun append(bytes: ByteArray) = append(bytes, bytes.size)

    fun append(bytes: ByteArray, count: Int) {
        val suffix = String(bytes, 0, count, charset)
        val normalizedSuffix = suffix.normalizeEOL()
        stringBuffer.append(normalizedSuffix)
    }

    suspend fun flush() {
        val lines = arrayListOf<String>()
        synchronized(lock) {
            val bufferLines = stringBuffer.lines()
            when (bufferLines.size) {
                0 -> Unit
                1 -> Unit
                else -> {
                    val lastLine = bufferLines.last()
                    stringBuffer.delete(0, stringBuffer.length)
                    if (!lastLine.isBlank()) {
                        stringBuffer.append(lastLine)
                    }
                    lines.addAll(bufferLines.subList(0, bufferLines.size - 1))
                }
            }
        }
        lines.forEach {
            onLine(it)
        }
    }

    suspend fun drain() {
        // ensure the last line will be flushed
        synchronized(lock) {
            if (stringBuffer.isNotEmpty()) {
                val last = stringBuffer.last()
                if (last != '\r' && last != '\n') {
                    stringBuffer.append('\n')
                }
            }
        }
        flush()
    }

    override fun close() {
        runBlocking {
            drain()
        }
    }
}

// replaces both \r and \r\n with a single \n
private fun String.normalizeEOL(): String {
    return replace("\\r\\n|\\r", "\n")
}
