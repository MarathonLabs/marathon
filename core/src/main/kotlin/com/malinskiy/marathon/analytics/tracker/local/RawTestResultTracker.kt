package com.malinskiy.marathon.analytics.tracker.local

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.malinskiy.marathon.analytics.tracker.NoOpTracker
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager

class RawTestResultTracker(private val fileManager: FileManager,
                                    private val gson: Gson) : NoOpTracker() {

    var testResults: MutableList<RawTestRun> = mutableListOf()

    override fun terminate() {
        super.terminate()
        val outputFile = fileManager.createTestResultFile("raw.json")
        outputFile.writeText(gson.toJson(testResults))
    }

    override fun trackRawTestRun(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {
        testResults.add(RawTestRun(
                testResult.test.pkg,
                testResult.test.clazz,
                testResult.test.method,
                device.serialNumber,
                testResult.status,
                testResult.isIgnored,
                testResult.isSuccess,
                testResult.startTime,
                testResult.durationMillis()
        ))
    }

    data class RawTestRun(@SerializedName("package") val pkg: String,
                          @SerializedName("class") val clazz: String,
                          @SerializedName("method") val method: String,
                          @SerializedName("deviceSerial") val deviceSerial: String,
                          @SerializedName("status") val status: TestStatus,
                          @SerializedName("ignored") val ignored: Boolean,
                          @SerializedName("success") val success: Boolean,
                          @SerializedName("timestamp") val timestamp: Long,
                          @SerializedName("duration") val duration: Long)
}
