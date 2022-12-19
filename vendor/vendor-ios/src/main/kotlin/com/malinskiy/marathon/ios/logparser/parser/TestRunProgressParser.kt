package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.logparser.TestEventProducer
import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.test.TestEvent
import com.malinskiy.marathon.ios.test.TestFailed
import com.malinskiy.marathon.ios.test.TestPassed
import com.malinskiy.marathon.ios.test.TestStarted
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer
import kotlin.math.roundToInt

class TestRunProgressParser(
    private val timer: Timer,
    private val packageNameFormatter: PackageNameFormatter,
) : TestEventProducer {

    val logger = MarathonLogging.logger(TestRunProgressParser::class.java.simpleName)

    val TEST_CASE_STARTED = """Test Case '-\[([a-zA-Z0-9_.]+)\.([a-zA-Z0-9_]+) ([a-zA-Z0-9_]+)]' started\.""".toRegex()
    val TEST_CASE_FINISHED =
        """Test Case '-\[([a-zA-Z0-9_.]+)\.([a-zA-Z0-9_]+) ([a-zA-Z0-9_]+)]' (passed|failed) \(([\d\.]+) seconds\)\.""".toRegex()

    override fun process(line: String): List<TestEvent>? {
        return if (line.matches(TEST_CASE_STARTED)) {
            parseTestStarted(line)?.let { listOf(it) }
        } else if (line.matches(TEST_CASE_FINISHED)) {
            parseTestFinished(line)?.let { listOf(it) }
        } else {
            null
        }
    }
    
    private fun parseTestFinished(line: String): TestEvent? {
        val matchResult = TEST_CASE_FINISHED.find(line)
        val pkg = packageNameFormatter.format(matchResult?.groups?.get(1)?.value)
        val clazz = matchResult?.groups?.get(2)?.value
        val method = matchResult?.groups?.get(3)?.value
        val result = matchResult?.groups?.get(4)?.value
        val duration = matchResult?.groups?.get(5)?.value?.toFloat()

        logger.debug { "Test $pkg.$clazz.$method finished with result <$result> after $duration seconds" }

        if (pkg != null && clazz != null && method != null && result != null && duration != null) {
            val test = Test(pkg, clazz, method, emptyList())

            val endTime = timer.currentTimeMillis()
            val startTime = endTime - (duration * 1000).roundToInt()

            return when (result) {
                "passed" -> {
                    TestPassed(test, startTime, endTime)
                }

                "failed" -> {
                    TestFailed(test, startTime, endTime)
                }

                else -> {
                    logger.error { "Unknown result $result for test $pkg.$clazz.$method" }
                    null
                }
            }
        }
        return null
    }

    private fun parseTestStarted(line: String): TestStarted? {
        val matchResult = TEST_CASE_STARTED.find(line)
        val pkg = packageNameFormatter.format(matchResult?.groups?.get(1)?.value)
        val clazz = matchResult?.groups?.get(2)?.value
        val method = matchResult?.groups?.get(3)?.value

        return if (pkg != null && clazz != null && method != null) {
            val test = Test(pkg, clazz, method, emptyList())
            logger.trace { "Test $pkg.$clazz.$method started" }
            TestStarted(test)
        } else {
            null
        }
    }
}
