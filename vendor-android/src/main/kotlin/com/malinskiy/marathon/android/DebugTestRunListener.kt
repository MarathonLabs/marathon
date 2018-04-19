package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier

class DebugTestRunListener(val device : IDevice) : ITestRunListener{
    override fun testRunStarted(runName: String?, testCount: Int) {
        println("testRunStarted ${device.serialNumber}")
    }

    override fun testStarted(test: TestIdentifier?) {
        println("testStarted ${device.serialNumber} test = $test")
    }

    override fun testAssumptionFailure(test: TestIdentifier?, trace: String?) {
        println("testAssumptionFailure ${device.serialNumber} test = $test trace = $trace")
    }

    override fun testRunStopped(elapsedTime: Long) {
        println("testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime")
    }

    override fun testFailed(test: TestIdentifier?, trace: String?) {
        println("testFailed ${device.serialNumber} test = $test trace = $trace")
    }

    override fun testEnded(test: TestIdentifier?, testMetrics: MutableMap<String, String>?) {
        println("testEnded ${device.serialNumber} test = $test")
    }

    override fun testIgnored(test: TestIdentifier?) {
        println("testIgnored ${device.serialNumber} test = $test")
    }

    override fun testRunFailed(errorMessage: String?) {
        println("testRunFailed ${device.serialNumber} errorMessage = $errorMessage")
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
        println("testRunEnded elapsedTime $elapsedTime")
    }
}