package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import com.malinskiy.marathon.ios.logparser.StreamingLogParser
import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.TestRunListener
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TestRunProgressParser(private val timer: Timer,
                            private val packageNameFormatter: PackageNameFormatter,
                            private val listeners: Collection<TestRunListener>) : StreamingLogParser {

    override fun close() {
        listeners.forEach { it.batchFinished() }
    }

    val logger = MarathonLogging.logger(TestRunProgressParser::class.java.simpleName)

    val TEST_CASE_STARTED = """Test Case '-\[([a-zA-Z0-9_.]+)\.([a-zA-Z0-9_]+) ([a-zA-Z0-9_]+)]' started\.""".toRegex()
    val TEST_CASE_FINISHED = """Test Case '-\[([a-zA-Z0-9_.]+)\.([a-zA-Z0-9_]+) ([a-zA-Z0-9_]+)]' (passed|failed) \(([\d\.]+) seconds\)\.""".toRegex()
    val TEST_CASE_CRASHED = """^.*Restarting after unexpected exit or crash in ([a-zA-Z0-9_.]+)/([a-zA-Z0-9_]+)\(\); summary will include totals from previous launches.*$""".toRegex()

    override fun onLine(line: String) {
        if (line.matches(TEST_CASE_STARTED)) {
            notifyTestStarted(line)
        } else if (line.matches(TEST_CASE_FINISHED)) {
            notifyTestFinished(line)
        } else if (line.matches(TEST_CASE_CRASHED)) {
            notifyTestCrashed(line)
        }
    }

    private fun notifyTestCrashed(line: String) {
        val matchResult = TEST_CASE_CRASHED.find(line)
        val clazz = matchResult?.groups?.get(1)?.value
        val method = matchResult?.groups?.get(2)?.value

        if (clazz != null && method != null) {
            val key = TestKey(clazz = clazz, method = method)
            testStartTimesCollector.remove(key)?.let { (test, startTime) ->
                val endTime = timer.currentTimeMillis()
                logger.debug { "Test $clazz.$method crashed. Reporting estimated duration of ${endTime - startTime}ms" }

                listeners.forEach { it.testFailed(test, startTime, endTime) }
            }
        }
    }

    private fun notifyTestFinished(line: String) {
        val matchResult = TEST_CASE_FINISHED.find(line)
        val pkg = packageNameFormatter.format(matchResult?.groups?.get(1)?.value)
        val clazz = matchResult?.groups?.get(2)?.value
        val method = matchResult?.groups?.get(3)?.value
        val result = matchResult?.groups?.get(4)?.value
        val duration = matchResult?.groups?.get(5)?.value?.toFloat()

        logger.debug { "Test $pkg.$clazz.$method finished with result <$result> after $duration seconds" }

        if (pkg != null && clazz != null && method != null && result != null && duration != null) {
            val test = Test(pkg, clazz, method, emptyList())

            testStartTimesCollector.remove(TestKey(test))

            val endTime = timer.currentTimeMillis()
            val startTime = endTime - Math.round(duration * 1000)

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

    private fun notifyTestStarted(line: String) {
        val matchResult = TEST_CASE_STARTED.find(line)
        val pkg = packageNameFormatter.format(matchResult?.groups?.get(1)?.value)
        val clazz = matchResult?.groups?.get(2)?.value
        val method = matchResult?.groups?.get(3)?.value

        if (pkg != null && clazz != null && method != null) {
            val test = Test(pkg, clazz, method, emptyList())
            logger.trace { "Test $pkg.$clazz.$method started" }

            val key = TestKey(test)
            testStartTimesCollector[key]?.let { (test, startTime) ->
                logger.error("Found a previous value saving a test start time: $test to $startTime. Overwriting.")
            }
            testStartTimesCollector[key] = test to timer.currentTimeMillis()
            listeners.forEach { it.testStarted(test) }
        }
    }

    private data class TestKey(val clazz: String, val method: String) {
        constructor(test: Test): this(clazz = test.clazz, method = test.method)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val test = other as TestKey
            return clazz == test.clazz &&
                    method == test.method
        }

        override fun hashCode(): Int {
            return Objects.hash(clazz, method)
        }
    }
    private val testStartTimesCollector = mutableMapOf<TestKey, Pair<Test, Long>>()
}