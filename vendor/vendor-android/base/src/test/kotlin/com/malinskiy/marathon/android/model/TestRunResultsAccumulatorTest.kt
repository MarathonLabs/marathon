package com.malinskiy.marathon.android.model

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test

class TestRunResultsAccumulatorTest {
    @Test
    fun testDefault() {
        val testRunResultsAccumulator = TestRunResultsAccumulator(mock())
        assertThat(testRunResultsAccumulator.apply {
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
        }.testResults)
            .containsOnly(
                test1 to AndroidTestResult(status = AndroidTestStatus.PASSED, 0, 0, null, mapOf("metric" to "value")),
                test2 to AndroidTestResult(status = AndroidTestStatus.FAILURE, 0, 0, "trace", null),
                test3 to AndroidTestResult(status = AndroidTestStatus.IGNORED, 0, 0, null, null),
                test4 to AndroidTestResult(status = AndroidTestStatus.ASSUMPTION_FAILURE, 0, 0, "trace", null),
                test5 to AndroidTestResult(status = AndroidTestStatus.INCOMPLETE, 0, 0, null, null),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isTrue()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isTrue()
        assertThat(testRunResultsAccumulator.isRunFailure).isFalse()

        assertThat(testRunResultsAccumulator.numCompleteTests).isEqualTo(4)
        assertThat(testRunResultsAccumulator.completedTests).containsOnly(
            test1, test2, test3, test4
        )
        assertThat(testRunResultsAccumulator.getNumTestsInState(AndroidTestStatus.PASSED)).isEqualTo(1)
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
        }.testResults)
            .containsOnly(
                test1 to AndroidTestResult(status = AndroidTestStatus.PASSED, 0, 0, null, mapOf("metric" to "value")),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isFalse()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isFalse()
        assertThat(testRunResultsAccumulator.isRunFailure).isTrue()

        assertThat(testRunResultsAccumulator.getNumTestsInState(AndroidTestStatus.FAILURE)).isEqualTo(0)
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
        }.testResults)
            .containsOnly(
                test1 to AndroidTestResult(status = AndroidTestStatus.PASSED, 0, 0, null, mapOf("metric" to "value")),
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
        }.testResults)
            .containsOnly(
                test1 to AndroidTestResult(status = AndroidTestStatus.FAILURE, 0, 0, "trace", null),
                test2 to AndroidTestResult(status = AndroidTestStatus.PASSED, 0, 0, null, emptyMap()),
            )

        assertThat(testRunResultsAccumulator.isRunComplete).isTrue()
        assertThat(testRunResultsAccumulator.hasFailedTests()).isTrue()
        assertThat(testRunResultsAccumulator.isRunFailure).isFalse()
    }

    companion object {
        val test1 = TestIdentifier("com.example.Class", "method1")
        val test2 = TestIdentifier("com.example.Class", "method2")
        val test3 = TestIdentifier("com.example.Class", "method3")
        val test4 = TestIdentifier("com.example.Class", "method4")
        val test5 = TestIdentifier("com.example.Class", "method5")
    }
}
