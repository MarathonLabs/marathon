package com.malinskiy.marathon.android.model

import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer

/**
 * Holds results from a single test run.
 *
 *
 * Maintains an accurate count of tests, and tracks incomplete tests.
 *
 *
 * Not thread safe! The test* callbacks must be called in order
 */
class TestRunResultsAccumulator(private val timer: Timer) {

    val logger = MarathonLogging.logger { }

    var name: String = "not started"
        private set
    val testResults = LinkedHashMap<TestIdentifier, AndroidTestResult>()
    val runMetrics = HashMap<String, String>()
    var isRunComplete = false
    private var isCountDirty = false
    var elapsedTime: Long = 0
        private set

    private var statusCounts: Map<AndroidTestStatus, Int> = mutableMapOf()
    private var expectedTestCount: Int = 0

    /**
     * Return the run failure error message, `null` if run did not fail.
     */
    var runFailureMessage: String? = null
        private set

    val completedTests: Set<TestIdentifier>
        get() {
            val completedTests = LinkedHashSet<TestIdentifier>()
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
        expectedTestCount = testCount
    }

    fun testStarted(test: TestIdentifier) {
        testStarted(test, timer.currentTimeMillis())
    }

    fun testStarted(test: TestIdentifier, startTime: Long) {
        val res = AndroidTestResult()
        res.startTime = startTime
        addTestResult(test, res)
    }

    private fun addTestResult(test: TestIdentifier, testResult: AndroidTestResult) {
        isCountDirty = true
        testResults[test] = testResult
    }

    private fun updateTestResult(test: TestIdentifier, status: AndroidTestStatus, trace: String?) {
        var r: AndroidTestResult? = testResults[test]
        if (r == null) {
            logger.debug { "received test event without test start for ${test.className}#${test.testName}" }
            r = AndroidTestResult(startTime = timer.currentTimeMillis())
        }
        r.status = status
        r.stackTrace = trace
        addTestResult(test, r)
    }

    fun testFailed(test: TestIdentifier, trace: String) {
        updateTestResult(test, AndroidTestStatus.FAILURE, trace)
    }

    fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        updateTestResult(test, AndroidTestStatus.ASSUMPTION_FAILURE, trace)
    }

    fun testIgnored(test: TestIdentifier) {
        updateTestResult(test, AndroidTestStatus.IGNORED, null)
    }

    fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        testEnded(test, timer.currentTimeMillis(), testMetrics)
    }

    fun testEnded(test: TestIdentifier, endTime: Long, testMetrics: Map<String, String>) {
        var result: AndroidTestResult? = testResults[test]
        if (result == null) {
            result = AndroidTestResult(startTime = timer.currentTimeMillis())
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
        fillEndTime()
    }

    private fun fillEndTime() {
        testResults.values.filter { it.endTime == 0L }.forEach { it ->
            it.endTime = timer.currentTimeMillis()
        }
    }

    fun testRunStopped(elapsedTime: Long) {
        this.elapsedTime += elapsedTime
        isRunComplete = true
        fillEndTime()
    }

    fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        this.runMetrics.putAll(runMetrics)
        this.elapsedTime += elapsedTime
        isRunComplete = true
        fillEndTime()
    }
}
