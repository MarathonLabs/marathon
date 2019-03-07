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
    val TEST_CASE_CRASHED_AND_RESTARTED = """^.*Restarting after unexpected exit or crash in ([a-zA-Z0-9_.]+)/([a-zA-Z0-9_]+)\(\); summary will include totals from previous launches.*$""".toRegex()
    val TEST_CASE_CRASHED_AND_FAILED = """\t([a-zA-Z0-9_]+)\(\) encountered an error \(Crash.*\)$""".toRegex()

    override fun onLine(line: String) {
        if (line.matches(TEST_CASE_STARTED)) {
            notifyTestStarted(line)
        } else if (line.matches(TEST_CASE_FINISHED)) {
            notifyTestFinished(line)
        } else if (line.matches(TEST_CASE_CRASHED_AND_RESTARTED)) {
            notifyTestCrashedAndRestarted(line)
        } else if (line.matches(TEST_CASE_CRASHED_AND_FAILED)) {
            notifyTestCrashedAndFailed(line)
        }
    }

    private fun notifyTestCrashedAndRestarted(line: String) {
        val matchResult = TEST_CASE_CRASHED_AND_RESTARTED.find(line)
        val clazz = matchResult?.groups?.get(1)?.value
        val method = matchResult?.groups?.get(2)?.value

        if (clazz != null && method != null) {
            val test = currentTest
            val startTime = currentTestStartTime
            if (test != null && startTime != null) {
                if (method == test.method && clazz == test.clazz) {
                    val endTime = timer.currentTimeMillis()
                    logger.debug { "Test $clazz.$method crashed. Reporting a failure with estimated duration ${endTime - startTime}ms" }
                    listeners.forEach { it.testFailed(test, startTime, endTime) }
                } else {
                    logger.warn("Test $clazz.$method crashed, but it doesn't match recorded $test. It will be reported as incomplete.")
                }
            } else {
                logger.warn("Test $clazz.$method crashed, but its start time has not been recorded. It will be reported as incomplete.")
            }
        }
    }

    private fun notifyTestCrashedAndFailed(line: String) {
        val matchResult = TEST_CASE_CRASHED_AND_FAILED.find(line)
        val method = matchResult?.groups?.get(1)?.value

        if (method != null) {
            val test = currentTest
            val startTime = currentTestStartTime
            if (test != null && startTime != null) {
                if (method == test.method) {
                    val endTime = timer.currentTimeMillis()
                    logger.debug { "Test $method crashed. Reporting a failure with estimated duration ${endTime - startTime}ms" }
                    listeners.forEach { it.testFailed(test, startTime, endTime) }
                } else {
                    logger.warn("Test $method crashed, but it doesn't match recorded $test. It will be reported as incomplete.")
                }
            } else {
                logger.warn("Test $method crashed, but its start time has not been recorded. It will be reported as incomplete.")
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

            currentTest?.let {
                if (it != test) {
                    logger.error("Current test $it started at $currentTestStartTime does not match finishing test $test. It will be discarded and reported as incomplete.")
                }
            }
            currentTest = null

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

            currentTest?.let {
                logger.error("Current test $it previously started at $currentTestStartTime. It will be discarded and reported as incomplete.")
            }
            currentTest = test
            currentTestStartTime = timer.currentTimeMillis()

            listeners.forEach { it.testStarted(test) }
        }
    }

    private var currentTest: Test? = null
    private var currentTestStartTime: Long? = null
}