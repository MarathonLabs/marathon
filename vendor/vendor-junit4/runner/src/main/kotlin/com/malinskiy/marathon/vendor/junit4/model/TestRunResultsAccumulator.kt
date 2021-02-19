package com.malinskiy.marathon.vendor.junit4.model

import com.malinskiy.marathon.log.MarathonLogging
import java.util.*

/**
 * Holds results from a single test run.
 *
 *
 * Maintains an accurate count of tests, and tracks incomplete tests.
 *
 *
 * Not thread safe! The test* callbacks must be called in order
 */
class TestRunResultsAccumulator {

    val logger = MarathonLogging.logger { }

    var name: String = "not started"
        private set
    val testResults = LinkedHashMap<TestIdentifier, JUnit4TestResult>()
    private val runMetrics = HashMap<String, String>()
    var isRunComplete = false
    var isCountDirty = false
    var elapsedTime: Long = 0
        private set

    private var statusCounts: Map<JUnit4TestStatus, Int> = mutableMapOf()

    /**
     * Return the run failure error message, `null` if run did not fail.
     */
    var runFailureMessage: String? = null
        private set

    var aggregateMetrics = false

    val completedTests: Set<TestIdentifier>
        get() {
            val completedTests = LinkedHashSet<TestIdentifier>()
            for ((key, value) in testResults) {
                if (value.status != JUnit4TestStatus.INCOMPLETE) {
                    completedTests.add(key)
                }
            }
            return completedTests
        }

    val isRunFailure: Boolean
        get() = runFailureMessage != null

    val numTests: Int
        get() = testResults.size

    val numCompleteTests: Int
        get() = numTests - getNumTestsInState(JUnit4TestStatus.INCOMPLETE)

    /**
     * Return total number of tests in a failure state (failed, assumption failure)
     */
    val numAllFailedTests: Int
        get() = getNumTestsInState(JUnit4TestStatus.FAILURE) + getNumTestsInState(JUnit4TestStatus.ASSUMPTION_FAILURE)

    /**
     * Gets the number of tests in given state for this run.
     */
    fun getNumTestsInState(status: JUnit4TestStatus): Int {
        if (isCountDirty) {
            statusCounts = testResults.values.groupingBy { it.status }.eachCount()
        }

        return statusCounts[status] ?: 0
    }

    /**
     * @return `true` if test run had any failed or error tests.
     */
    fun hasFailedTests(): Boolean {
        return numAllFailedTests > 0
    }


    fun testRunStarted(runName: String, testCount: Int) {
        name = runName
        isRunComplete = false
        runFailureMessage = null
    }

    fun testStarted(test: TestIdentifier) {
        testStarted(test, System.currentTimeMillis())
    }

    fun testStarted(test: TestIdentifier, startTime: Long) {
        val res = JUnit4TestResult()
        res.startTime = startTime
        addTestResult(test, res)
    }

    private fun addTestResult(test: TestIdentifier, testResult: JUnit4TestResult) {
        isCountDirty = true
        testResults[test] = testResult
    }

    private fun updateTestResult(test: TestIdentifier, status: JUnit4TestStatus, trace: String?) {
        var r: JUnit4TestResult? = testResults[test]
        if (r == null) {
            logger.debug { "received test event without test start for ${test.className}#${test.testName}" }
            r = JUnit4TestResult()
        }
        r.status = status
        r.stackTrace = trace
        addTestResult(test, r)
    }

    fun testFailed(test: TestIdentifier, trace: String) {
        updateTestResult(test, JUnit4TestStatus.FAILURE, trace)
    }

    fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        updateTestResult(test, JUnit4TestStatus.ASSUMPTION_FAILURE, trace)
    }

    fun testIgnored(test: TestIdentifier) {
        updateTestResult(test, JUnit4TestStatus.IGNORED, null)
    }

    fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        testEnded(test, System.currentTimeMillis(), testMetrics)
    }

    fun testEnded(test: TestIdentifier, endTime: Long, testMetrics: Map<String, String>) {
        var result: JUnit4TestResult? = testResults[test]
        if (result == null) {
            result = JUnit4TestResult()
        }
        if (result.status == JUnit4TestStatus.INCOMPLETE) {
            result.status = JUnit4TestStatus.PASSED
        }
        result.endTime = endTime
        result.metrics = testMetrics
        addTestResult(test, result)
    }

    fun testRunFailed(errorMessage: String) {
        runFailureMessage = errorMessage
        fillEndTime()
    }

    private fun fillEndTime() {
        testResults.values.filter { it.endTime == 0L }.forEach { it ->
            it.endTime = System.currentTimeMillis()
        }
    }

    fun testRunStopped(elapsedTime: Long) {
        this.elapsedTime += elapsedTime
        isRunComplete = true
        fillEndTime()
    }

    fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        if (aggregateMetrics) {
            for ((key, value) in runMetrics) {
                combineValues(runMetrics[key], value)?.let {
                    this.runMetrics[key] = it
                }
            }
        } else {
            this.runMetrics.putAll(runMetrics)
        }
        this.elapsedTime += elapsedTime
        isRunComplete = true
        fillEndTime()
    }

    /**
     * Combine old and new metrics value
     *
     * @param existingValue
     * @param newValue
     * @return the combination of the two string as Long or Double value.
     */
    private fun combineValues(existingValue: String?, newValue: String): String? {
        if (existingValue != null) {
            try {
                val existingLong = java.lang.Long.parseLong(existingValue)
                val newLong = java.lang.Long.parseLong(newValue)
                return java.lang.Long.toString(existingLong + newLong)
            } catch (e: NumberFormatException) {
                // not a long, skip to next
            }

            try {
                val existingDouble = java.lang.Double.parseDouble(existingValue)
                val newDouble = java.lang.Double.parseDouble(newValue)
                return java.lang.Double.toString(existingDouble + newDouble)
            } catch (e: NumberFormatException) {
                // not a double either, fall through
            }
        }
        // default to overriding existingValue
        return newValue
    }
}
