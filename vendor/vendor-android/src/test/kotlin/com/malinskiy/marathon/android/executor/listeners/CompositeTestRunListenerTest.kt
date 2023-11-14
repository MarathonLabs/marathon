package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CompositeTestRunListenerTest {
    @Test
    fun testDefault() {
        val mock = mock<TestResultsListener>()
        val listener = CompositeTestRunListener(listOf(mock))

        runBlocking {
            listener.apply {
                beforeTestRun()
                verify(mock, times(1)).beforeTestRun()

                testRunStarted("test", 1)
                verify(mock, times(1)).testRunStarted("test", 1)

                testStarted(test)
                verify(mock, times(1)).testStarted(test)

                testFailed(test, "trace")
                verify(mock, times(1)).testFailed(test, "trace")

                testEnded(test, emptyMap())
                verify(mock, times(1)).testEnded(test, emptyMap())

                testAssumptionFailure(test, "trace")
                verify(mock, times(1)).testAssumptionFailure(test, "trace")

                testIgnored(test)
                verify(mock, times(1)).testIgnored(test)

                testRunEnded(1000, emptyMap())
                verify(mock, times(1)).testRunEnded(1000, emptyMap())

                testRunStopped(300)
                verify(mock, times(1)).testRunStopped(300)

                testRunFailed("error")
                verify(mock, times(1)).testRunFailed("error")
            }
        }
    }

    companion object {
        val test = TestIdentifier("com.example.Clazz", "test")
    }
}
