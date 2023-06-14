package com.malinskiy.marathon.execution.result

import com.malinskiy.marathon.execution.TestStatus
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue

class TestRunResultsAccumulatorTest {
    @Test
    fun testDefault() {
        val testRunResultsAccumulator = TestRunResultsAccumulator(mock())
        val temporalTestResults = testRunResultsAccumulator.apply {
            testRunStarted("testing", 3)
            testStarted(test1)
            testEnded(test1, mapOf("metric" to "value"))

            testStarted(test2)
            testFailed(test2, "trace")

            testStarted(test3)
            testIgnored(test3)

            testStarted(test4)
            testAssumptionFailure(test4, "trace")

            testStarted(test5)

            testRunEnded(1234, mapOf("metric1" to "value1"))
        }.temporalTestResults
        assertThat(temporalTestResults)
            .containsOnly(
                test1 to TemporalTestResult(status = TestStatus.PASSED, 0, 0, null, mapOf("metric" to "value")),
                test2 to TemporalTestResult(status = TestStatus.FAILURE, 0, 0, "trace", null),
                test3 to TemporalTestResult(status = TestStatus.IGNORED, 0, 0, null, null),
                test4 to TemporalTestResult(status = TestStatus.ASSUMPTION_FAILURE, 0, 0, "trace", null),
                test5 to TemporalTestResult(status = TestStatus.INCOMPLETE, 0, 0, null, null),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isTrue()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isTrue()
        assertThat(testRunResultsAccumulator.isRunFailure).isFalse()

        assertThat(testRunResultsAccumulator.numCompleteTests).isEqualTo(4)
        assertThat(testRunResultsAccumulator.completedTests).containsOnly(
            test1, test2, test3, test4
        )
        assertThat(testRunResultsAccumulator.getNumTestsInState(TestStatus.PASSED)).isEqualTo(1)
        assertThat(testRunResultsAccumulator.elapsedTime).isEqualTo(1234L)
    }

    @Test
    fun testRunFailed() {
        val testRunResultsAccumulator = TestRunResultsAccumulator(mock())
        assertThat(testRunResultsAccumulator.apply {
            testRunStarted("testing", 3)
            testStarted(test1)
            testEnded(test1, mapOf("metric" to "value"))

            testRunFailed("Problems are everywhere")
        }.temporalTestResults)
            .containsOnly(
                test1 to TemporalTestResult(status = TestStatus.PASSED, 0, 0, null, mapOf("metric" to "value")),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isFalse()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isFalse()
        assertThat(testRunResultsAccumulator.isRunFailure).isTrue()

        assertThat(testRunResultsAccumulator.getNumTestsInState(TestStatus.FAILURE)).isEqualTo(0)
        assertThat(testRunResultsAccumulator.runFailureMessage).isEqualTo("Problems are everywhere")
    }

    @Test
    fun testRunStopped() {
        val testRunResultsAccumulator = TestRunResultsAccumulator(mock())
        assertThat(testRunResultsAccumulator.apply {
            testRunStarted("testing", 3)
            testStarted(test1)
            testEnded(test1, mapOf("metric" to "value"))

            testRunStopped(300)
        }.temporalTestResults)
            .containsOnly(
                test1 to TemporalTestResult(status = TestStatus.PASSED, 0, 0, null, mapOf("metric" to "value")),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isTrue()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isFalse()
        assertThat(testRunResultsAccumulator.isRunFailure).isFalse()
    }

    @Test
    fun testOnlyTestEnded() {
        val testRunResultsAccumulator = TestRunResultsAccumulator(mock())
        assertThat(testRunResultsAccumulator.apply {
            testRunStarted("testing", 3)
            testFailed(test1, "trace")
            testEnded(test2, emptyMap())
            testRunEnded(300, emptyMap())
        }.temporalTestResults)
            .containsOnly(
                test1 to TemporalTestResult(status = TestStatus.FAILURE, 0, 0, "trace", null),
                test2 to TemporalTestResult(status = TestStatus.PASSED, 0, 0, null, emptyMap()),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isTrue()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isTrue()
        assertThat(testRunResultsAccumulator.isRunFailure).isFalse()
    }

    companion object {
        val test1 = MarathonTest("com.example", "Class", "method1", emptyList())
        val test2 = MarathonTest("com.example", "Class", "method2", emptyList())
        val test3 = MarathonTest("com.example", "Class", "method3", emptyList())
        val test4 = MarathonTest("com.example", "Class", "method4", emptyList())
        val test5 = MarathonTest("com.example", "Class", "method5", emptyList())
    }
}
