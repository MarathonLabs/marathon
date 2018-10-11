package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.TestRunListener
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer

class TestRunProgressParser(private val timer: Timer,
                            private val listeners: Collection<TestRunListener>,
                            private val packageNameFormatter: PackageNameFormatter) : StreamingLogParser {

    override fun close() {
        listeners.forEach { it.batchFinished() }
    }

    val logger = MarathonLogging.logger(TestRunProgressParser::class.java.simpleName)

    val TEST_CASE_STARTED = """Test Case '-\[([a-zA-Z0-9_.]+)\.([a-zA-Z0-9_]+) ([a-zA-Z0-9_]+)]' started\.""".toRegex()
    val TEST_CASE_FINISHED = """Test Case '-\[([a-zA-Z0-9_.]+)\.([a-zA-Z0-9_]+) ([a-zA-Z0-9_]+)]' (passed|failed) \(([\d\.]+) seconds\)\.""".toRegex()

    override fun onLine(line: String) {
        if (line.matches(TEST_CASE_STARTED)) {
            notifyTestStarted(line)
        } else if (line.matches(TEST_CASE_FINISHED)) {
            notifyTestFinished(line)
        }
    }

    fun notifyTestFinished(line: String) {
        val matchResult = TEST_CASE_FINISHED.find(line)
        val pkg = packageNameFormatter.format(matchResult?.groups?.get(1)?.value)
        val clazz = matchResult?.groups?.get(2)?.value
        val method = matchResult?.groups?.get(3)?.value
        val result = matchResult?.groups?.get(4)?.value
        val duration = matchResult?.groups?.get(5)?.value?.toFloat()

        logger.debug { "Test $clazz.$method could be finished with result $result" }

        if (pkg != null && clazz != null && method != null && result != null && duration != null) {
            val test = Test(pkg, clazz, method, emptyList())

            val endTime = timer.currentTimeMillis()
            val startTime = endTime - Math.round(duration * 1000)

            logger.debug { "Test $clazz.$method finished with result <$result> after $duration seconds" }

            when (result) {
                "passed" -> {
                    listeners.forEach { it.testPassed(test, startTime, endTime) }
                }
                "failed" -> {
                    listeners.forEach { it.testFailed(test, startTime, endTime) }
                }
                else -> logger.error { "Unknown result $result for test $pkg.$clazz.$method" }
            }
        }
    }

    fun notifyTestStarted(line: String) {
        val matchResult = TEST_CASE_STARTED.find(line)
        val pkg = packageNameFormatter.format(matchResult?.groups?.get(1)?.value)
        val clazz = matchResult?.groups?.get(2)?.value
        val method = matchResult?.groups?.get(3)?.value

        logger.debug { "Test $clazz.$method could be started" }

        if (pkg != null && clazz != null && method != null) {
            val test = Test(pkg, clazz, method, emptyList())
            logger.debug { "Test $clazz.$method started" }
            listeners.forEach { it.testStarted(test) }
        }
    }
}
