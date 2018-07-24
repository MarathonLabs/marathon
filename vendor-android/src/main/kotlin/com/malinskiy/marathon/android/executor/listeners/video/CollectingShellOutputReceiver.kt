package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.IShellOutputReceiver

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

class CollectingShellOutputReceiver : IShellOutputReceiver {
    private val sb = StringBuilder()

    fun output(): String = sb.toString()

    override fun addOutput(byteArray: ByteArray, offset: Int, length: Int) {
        val latin1Charset = Charset.forName("ISO-8859-1")
        val charBuffer = latin1Charset.decode(ByteBuffer.wrap(byteArray))
        sb.append(charBuffer.toString(), offset, length)
    }

    override fun flush() {}

    override fun isCancelled(): Boolean {
        return false
    }
}
