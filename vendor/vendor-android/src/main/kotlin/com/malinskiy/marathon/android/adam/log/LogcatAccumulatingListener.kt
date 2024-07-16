package com.malinskiy.marathon.android.adam.log

import com.malinskiy.marathon.android.logcat.LogcatListener
import com.malinskiy.marathon.android.logcat.LogcatProducer

class LogcatAccumulatingListener(
    private val logcatProducer: LogcatProducer,
) : LogcatListener {
    private val stringBuffer = StringBuffer(4096)
    private val allowlist = setOf("AndroidRuntime")

    override suspend fun onMessage(msg: LogCatMessage) {
        if (allowlist.contains(msg.tag)) {
            when (msg.logLevel) {
                Log.LogLevel.ERROR, Log.LogLevel.WARN -> stringBuffer.appendLine("${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}\"")
                else -> Unit
            }
        }
    }

    fun setup() {
        logcatProducer.addLogcatListener(this)
    }

    fun finish() {
        logcatProducer.removeLogcatListener(this)
    }

    fun start() {
        stringBuffer.reset()
    }

    fun stop(): String {
        val log = stringBuffer.toString()
        stringBuffer.reset()
        return log
    }

    private fun StringBuffer.reset() {
        delete(0, length)
    }
}
