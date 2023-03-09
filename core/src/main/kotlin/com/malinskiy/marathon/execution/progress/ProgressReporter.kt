package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName

class ProgressReporter(private val batch: TestBatch, private val poolId: DevicePoolId, private val device: DeviceInfo) {
    fun testStarted(test: Test) {
        println("${batch.id} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} started")
    }

    fun testFailed(test: Test) {
        println("${batch.id} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} failed")
    }

    fun testPassed(test: Test) {
        println("${batch.id} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} passed")
    }

    fun testIgnored(test: Test) {
        println("${batch.id} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} ignored")
    }
}
