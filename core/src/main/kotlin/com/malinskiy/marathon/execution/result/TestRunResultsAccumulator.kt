package com.malinskiy.marathon.execution.result

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toSafeTestName
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
    val temporalTestResults = LinkedHashMap<Test, TemporalTestResult>()
    val runMetrics = HashMap<String, String>()
    var isRunComplete = false
    private var isCountDirty = false
    var elapsedTime: Long = 0
        private set

    private var statusCounts: Map<TestStatus, Int> = mutableMapOf()
    private var expectedTestCount: Int = 0
    private val creationTime = timer.currentTimeMillis()

    /**
     * Return the run failure error message, `null` if run did not fail.
     */
    var runFailureMessage: String? = null
        private set

    val completedTests: Set<Test>
        get() {
            val completedTests = LinkedHashSet<Test>()
            for ((key, value) in temporalTestResults) {
                if (value.status != TestStatus.INCOMPLETE) {
                    completedTests.add(key)
                }
            }
            return completedTests
        }

    val isRunFailure: Boolean
        get() = runFailureMessage != null

    val numTests: Int
        get() = temporalTestResults.size

    val numCompleteTests: Int
        get() = numTests - getNumTestsInState(TestStatus.INCOMPLETE)

    /**
     * Return total number of tests in a failure state (failed, assumption failure)
     */
    val numAllFailedTests: Int
        get() = getNumTestsInState(TestStatus.FAILURE) + getNumTestsInState(TestStatus.ASSUMPTION_FAILURE)

    /**
     * Gets the number of tests in given state for this run.
     */
    fun getNumTestsInState(status: TestStatus): Int {
        if (isCountDirty) {
            statusCounts = temporalTestResults.values.groupingBy { it.status }.eachCount()
        }

        return statusCounts[status] ?: 0
    }

    /**
     * @return `true` if test run had any failed or error tests.
     */
    fun hasFailedTests(): Boolean {
        return numAllFailedTests > 0
    }

    /**
     * Fills in the missing information about
     */
    fun createUncompletedTestResults(
        tests: Collection<Test>,
        testBatch: TestBatch,
        deviceInfo: DeviceInfo,
    ): Collection<TestResult> {
        val lastCompletedTestEndTime = temporalTestResults
            .values
            .maxByOrNull { it.endTime }
            ?.endTime
            ?: creationTime

        return tests.map {
            TestResult(
                it,
                deviceInfo,
                testBatch.id,
                TestStatus.INCOMPLETE,
                lastCompletedTestEndTime,
                timer.currentTimeMillis(),
                runFailureMessage
            )
        }
    }

    fun testRunStarted(runName: String, testCount: Int) {
        name = runName
        isRunComplete = false
        runFailureMessage = null
        expectedTestCount = testCount
    }

    fun testStarted(test: Test) {
        testStarted(test, timer.currentTimeMillis())
    }

    fun testStarted(test: Test, startTime: Long) {
        val res = TemporalTestResult()
        res.startTime = startTime
        addTestResult(test, res)
    }

    private fun addTestResult(test: Test, temporalTestResult: TemporalTestResult) {
        isCountDirty = true
        temporalTestResults[test] = temporalTestResult
    }

    private fun updateTestResult(test: Test, status: TestStatus, trace: String? = null, startTime: Long? = null, endTime: Long? = null) {
        var r: TemporalTestResult? = temporalTestResults[test]
        if (r == null) {
            logger.debug { "received test event without test start for ${test.toSafeTestName()}" }
            r = TemporalTestResult(startTime = timer.currentTimeMillis())
        }
        r.status = status
        r.stackTrace = trace
        startTime?.let { r.startTime = it }
        endTime?.let { r.endTime = it }
        addTestResult(test, r)
    }

    fun testFailed(test: Test, trace: String, startTime: Long? = null, endTime: Long? = null) {
        updateTestResult(test, TestStatus.FAILURE, trace, startTime, endTime)
    }

    fun testAssumptionFailure(test: Test, trace: String) {
        updateTestResult(test, TestStatus.ASSUMPTION_FAILURE, trace)
    }

    fun testIgnored(test: Test) {
        updateTestResult(test, TestStatus.IGNORED)
    }

    fun testEnded(test: Test, testMetrics: Map<String, String>, startTime: Long? = null, endTime: Long = timer.currentTimeMillis()) {
        var result: TemporalTestResult? = temporalTestResults[test]
        if (result == null) {
            result = TemporalTestResult(startTime = startTime ?: timer.currentTimeMillis())
        }
        if (result.status == TestStatus.INCOMPLETE) {
            result.status = TestStatus.PASSED
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
        temporalTestResults.values.filter { it.endTime == 0L }.forEach { it ->
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
