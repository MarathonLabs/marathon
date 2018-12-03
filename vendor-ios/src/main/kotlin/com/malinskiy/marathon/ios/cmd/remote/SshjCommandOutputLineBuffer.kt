package com.malinskiy.marathon.ios.cmd.remote

import java.io.Closeable

class SshjCommandOutputLineBuffer(private val onLine: (String) -> Unit): Closeable {
    private val stringBuffer = StringBuffer(16384)

    fun append(bytes: ByteArray) = append(bytes, bytes.size)

    fun append(bytes: ByteArray, count: Int) =
        synchronized(stringBuffer) {
            stringBuffer.append(String(bytes, 0, count))
        }


    fun flush() {
        val lines = arrayListOf<String>()
        synchronized(stringBuffer) {
            stringBuffer.normalizeEOL()
            while (stringBuffer.isNotEmpty() && stringBuffer.contains('\n')) {
                val line = stringBuffer.takeWhile { it != '\n' }
                stringBuffer.deleteWhile { it != '\n' }.deleteCharAt(0)
                lines.add(line.toString())
            }
        }
        lines.forEach(onLine)
    }

    fun drain() {
        // ensure the last line will be flushed
        synchronized(stringBuffer) {
            if (stringBuffer.isNotEmpty()) {
                val last = stringBuffer.last()
                if (last != '\r' && last != '\n') {
                    stringBuffer.append('\n')
                }
            }
        }
        flush()
    }

    override fun close() = drain()
}

private fun StringBuffer.deleteWhile(predicate: (Char) -> Boolean): StringBuffer {
    while (this.isNotEmpty() && predicate(this.first())) {
        this.deleteCharAt(0)
    }
    return this
}

// replaces both \r and \r\n with a single \n
private fun StringBuffer.normalizeEOL(): StringBuffer {
    var index = 0
    while (index < length) {
        if (index + 1 < length && get(index) == '\r' && get(index+1) == '\n') {
            deleteCharAt(index)
        } else {
            if (get(index) == '\r') {
                setCharAt(index, '\n')
            }
            index ++
        }
    }
    return this
}
