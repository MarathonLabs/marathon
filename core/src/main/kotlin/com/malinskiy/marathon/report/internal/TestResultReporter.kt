package com.malinskiy.marathon.report.internal

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import java.io.FileReader

class TestResultReporter(private val fileManager: FileManager,
                         private val gson: Gson) {

    var testResults: MutableList<RawTestRun> = mutableListOf()

    fun testFinished(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        logTestExecution(testResult, device)

        reportTest(poolId, device, testResult)
    }

    fun flush() {
        val outputFile = fileManager.createTestResultFile("raw.json")
        outputFile.writeText(gson.toJson(testResults))
    }

    private fun logTestExecution(testResult: TestResult, device: Device) {
        // Don't report tests that didn't finish the execution
        if (testResult.status == TestStatus.INCOMPLETE) return

        testResults.add(RawTestRun(
                testResult.test.pkg,
                testResult.test.clazz,
                testResult.test.method,
                device.serialNumber,
                testResult.isIgnored,
                testResult.isSuccess,
                testResult.startTime
        ))
    }

    private fun reportTest(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        val file = fileManager.createFile(FileType.TEST_RESULT, poolId, device, testResult.test)
        file.writeText(gson.toJson(testResult))
    }

    fun readTests(poolId: DevicePoolId, device: DeviceInfo): List<TestResult> {
        return fileManager.getTestResultFilesForDevice(poolId, device.serialNumber).map {
            gson.fromJson(FileReader(it), TestResult::class.java)
        }
    }

    data class RawTestRun(@SerializedName("package") private val pkg: String,
                          @SerializedName("class") private val clazz: String,
                          @SerializedName("method") private val method: String,
                          @SerializedName("deviceSerial") private val deviceSerial: String,
                          @SerializedName("ignored") private val ignored: Boolean,
                          @SerializedName("success") private val success: Boolean,
                          @SerializedName("timestamp") private val timestamp: Long)
}
