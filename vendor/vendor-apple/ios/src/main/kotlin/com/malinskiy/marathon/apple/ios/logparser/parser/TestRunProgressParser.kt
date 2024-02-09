package com.malinskiy.marathon.apple.ios.logparser.parser

import com.malinskiy.marathon.apple.ios.logparser.TestEventProducer
import com.malinskiy.marathon.apple.ios.test.TestEvent
import com.malinskiy.marathon.apple.ios.test.TestFailed
import com.malinskiy.marathon.apple.ios.test.TestIgnored
import com.malinskiy.marathon.apple.ios.test.TestPassed
import com.malinskiy.marathon.apple.ios.test.TestStarted
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer
import kotlin.math.roundToInt

class TestRunProgressParser(
    private val timer: Timer,
    private val targetName: String,
) : TestEventProducer {

    val logger = MarathonLogging.logger(TestRunProgressParser::class.java.simpleName)

    val TEST_CASE_STARTED = """Test Case '-\[(.+) (.+)]' started\.""".toRegex()
    val TEST_CASE_FINISHED =
        """Test Case '-\[(.+) (.+)]' (passed|failed|skipped) \(([\d\.]+) seconds\)\.""".toRegex()

    /**
     * $1 = file
     * $2 = test_suite
     * $3 = test_case
     * $4 = reason
     */
    val FAILING_TEST_MATCHER = "(/.+:\\d+):\\serror:\\s[\\+\\-]\\[(.*)\\s(.*)\\]\\s:(\\s.*)".toRegex()
    
    private var failingTestLine: String? = null
    
    override fun process(line: String): List<TestEvent>? {
        return if (line.matches(TEST_CASE_STARTED)) {
            parseTestStarted(line)?.let { listOf(it) }
        } else if (line.matches(TEST_CASE_FINISHED)) {
            parseTestFinished(line)?.let { listOf(it) }
        } else if (line.matches(FAILING_TEST_MATCHER)) {
            failingTestLine = line
            null
        } else {
            null
        }
    }

    private fun parseFailingTest(line: String): String? {
        val matchResult = FAILING_TEST_MATCHER.find(line)
        val file = matchResult?.groups?.get(1)?.value?.trim()
        val testSuite = matchResult?.groups?.get(2)?.value?.trim()
        val testCase = matchResult?.groups?.get(3)?.value?.trim()
        val reason = matchResult?.groups?.get(4)?.value?.trim()
        
        if(file !== null && testSuite != null && testCase != null && reason != null) {
            return "$file:$reason"
        }
        return null
    }

    private fun parseTestFinished(line: String): TestEvent? {
        val matchResult = TEST_CASE_FINISHED.find(line)
        val pkgWithClass = matchResult?.groups?.get(1)?.value
        var pkg: String? = null
        var clazz: String? = null
        if (pkgWithClass != null) {
            if (pkgWithClass.contains('.')) {
                pkg = pkgWithClass.substringBeforeLast('.', missingDelimiterValue = "")
                clazz = pkgWithClass.substringAfter('.', missingDelimiterValue = pkgWithClass)
            } else {
                pkg = targetName
                clazz = pkgWithClass
            }
        }

        val method = matchResult?.groups?.get(2)?.value
        val result = matchResult?.groups?.get(3)?.value
        val duration = matchResult?.groups?.get(4)?.value?.toFloat()

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
                    val trace = failingTestLine?.let { 
                        parseFailingTest(it)
                    }
                    TestFailed(test, startTime, endTime, trace)
                }
                "skipped" -> {
                    TestIgnored(test, startTime, endTime)
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
        failingTestLine = null
        val matchResult = TEST_CASE_STARTED.find(line)
        val pkgWithClass = matchResult?.groups?.get(1)?.value
        var pkg: String? = null
        var clazz: String? = null
        if (pkgWithClass != null) {
            if (pkgWithClass.contains('.')) {
                pkg = pkgWithClass.substringBeforeLast('.', missingDelimiterValue = "")
                clazz = pkgWithClass.substringAfter('.', missingDelimiterValue = pkgWithClass)
            } else {
                pkg = targetName
                clazz = pkgWithClass
            }
        }
        val method = matchResult?.groups?.get(2)?.value

        return if (pkg != null && clazz != null && method != null) {
            val test = Test(pkg, clazz, method, emptyList())
            logger.trace { "Test $pkg.$clazz.$method started" }
            TestStarted(test)
        } else {
            null
        }
    }
}
