package com.malinskiy.marathon.android.model

import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

class TestRunResult {
    fun testRunStarted(runName: String, testCount: Int) {
    }

    fun testStarted(test: Test) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testFailed(test: Test, trace: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testAssumptionFailure(test: Test, trace: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testIgnored(test: Test) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testEnded(test: Test, testMetrics: Map<String, String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testRunFailed(errorMessage: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testRunStopped(elapsedTime: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getTestResults(): MutableMap<Test, TestResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getRunFailureMessage(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}