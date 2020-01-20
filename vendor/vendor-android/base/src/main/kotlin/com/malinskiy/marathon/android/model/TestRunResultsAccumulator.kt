package com.malinskiy.marathon.android.model

import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSimpleSafeTestName
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
    val testResults = LinkedHashMap<Test, AndroidTestResult>()
    private val runMetrics = HashMap<String, String>()
    var isRunComplete = false
    var isCountDirty = false
    var elapsedTime: Long = 0
        private set

    private var statusCounts: Map<AndroidTestStatus, Int> = mutableMapOf()

    /**
     * Return the run failure error message, `null` if run did not fail.
     */
    var runFailureMessage: String? = null
        private set

    var aggregateMetrics = false

    val completedTests: Set<Test>
        get() {
            val completedTests = LinkedHashSet<Test>()
            for ((key, value) in testResults) {
                if (value.status != AndroidTestStatus.INCOMPLETE) {
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
        get() = numTests - getNumTestsInState(AndroidTestStatus.INCOMPLETE)

    /**
     * Return total number of tests in a failure state (failed, assumption failure)
     */
    val numAllFailedTests: Int
        get() = getNumTestsInState(AndroidTestStatus.FAILURE) + getNumTestsInState(AndroidTestStatus.ASSUMPTION_FAILURE)

    /**
     * Gets the number of tests in given state for this run.
     */
    fun getNumTestsInState(status: AndroidTestStatus): Int {
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

    fun testStarted(test: Test) {
        testStarted(test, System.currentTimeMillis())
    }

    fun testStarted(test: Test, startTime: Long) {
        val res = AndroidTestResult()
        res.startTime = startTime
        addTestResult(test, res)
    }

    private fun addTestResult(test: Test, testResult: AndroidTestResult) {
        isCountDirty = true
        testResults[test] = testResult
    }

    private fun updateTestResult(test: Test, status: AndroidTestStatus, trace: String?) {
        var r: AndroidTestResult? = testResults[test]
        if (r == null) {
            logger.debug { "received test event without test start for ${test.toSimpleSafeTestName()}" }
            r = AndroidTestResult()
        }
        r.status = status
        r.stackTrace = trace
        addTestResult(test, r)
    }

    fun testFailed(test: Test, trace: String) {
        updateTestResult(test, AndroidTestStatus.FAILURE, trace)
    }

    fun testAssumptionFailure(test: Test, trace: String) {
        updateTestResult(test, AndroidTestStatus.ASSUMPTION_FAILURE, trace)
    }

    fun testIgnored(test: Test) {
        updateTestResult(test, AndroidTestStatus.IGNORED, null)
    }

    fun testEnded(test: Test, testMetrics: Map<String, String>) {
        testEnded(test, System.currentTimeMillis(), testMetrics)
    }

    fun testEnded(test: Test, endTime: Long, testMetrics: Map<String, String>) {
        var result: AndroidTestResult? = testResults[test]
        if (result == null) {
            result = AndroidTestResult()
        }
        if (result.status == AndroidTestStatus.INCOMPLETE) {
            result.status = AndroidTestStatus.PASSED
        }
        result.endTime = endTime
        result.metrics = testMetrics
        addTestResult(test, result)
    }

    fun testRunFailed(errorMessage: String) {
        runFailureMessage = errorMessage
    }

    fun testRunStopped(elapsedTime: Long) {
        this.elapsedTime += elapsedTime
        isRunComplete = true
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
