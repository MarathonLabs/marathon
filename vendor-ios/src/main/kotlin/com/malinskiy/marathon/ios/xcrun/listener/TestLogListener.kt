package com.malinskiy.marathon.ios.xcrun.listener

import com.malinskiy.marathon.ios.xcrun.StreamingLogParser
import com.malinskiy.marathon.test.Test

class TestLogListener : TestRunListener, StreamingLogParser {

    var buffer: StringBuffer? = null
    var lastLine: String? = null

    fun getLastLog() = buffer?.toString() ?: ""

    override fun onLine(line: String) {
        buffer?.appendln(line)
        lastLine = line
    }

    override fun batchFinished() {
    }

    override fun testPassed(test: Test, startTime: Long, endTime: Long) {}

    override fun testFailed(test: Test, startTime: Long, endTime: Long) {}

    override fun testStarted(test: Test) {
        buffer = StringBuffer(lastLine ?: "")
    }

    override fun close() {
    }
}
