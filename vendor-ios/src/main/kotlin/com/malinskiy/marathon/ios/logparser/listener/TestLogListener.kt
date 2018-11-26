package com.malinskiy.marathon.ios.logparser.listener

import com.malinskiy.marathon.ios.logparser.StreamingLogParser
import com.malinskiy.marathon.test.Test

class TestLogListener : TestRunListener, StreamingLogParser, TestLogCollector {

    private var buffer: StringBuffer? = null
    private var lastLine: String? = null

    override fun getLastLog() = buffer?.toString() ?: ""

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
