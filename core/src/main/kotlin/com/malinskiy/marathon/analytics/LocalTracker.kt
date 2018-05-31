package com.malinskiy.marathon.analytics

import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.report.internal.TestResultSerializer
import com.malinskiy.marathon.report.junit.JUnitReporter
import com.malinskiy.marathon.test.Test

class LocalTracker(private val fileManager: FileManager,
                   private val gson: Gson,
                   private val jUnitReporter: JUnitReporter,
                   private val testResultSerializer: TestResultSerializer) : Tracker {
    override fun trackTestStarted(test: Test, time: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun trackTestFinished(test: Test, success: Boolean, time: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun trackTestIgnored(test: Test) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        jUnitReporter.testFinished(poolId, device, testResult)
        testResultSerializer.testFinished(poolId, device, testResult)
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
        val json = gson.toJson(device.toDeviceInfo())
        fileManager.createFile(FileType.DEVICE_INFO, poolId, device).writeText(json)
    }

    override fun calculateTestExpectedTime(test: Test): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun calculateTestVariance(test: Test): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun calculateTestExpectedRetries(test: Test): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
