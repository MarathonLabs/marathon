package com.malinskiy.marathon.report

import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import java.io.File
import java.io.FileReader

class SummaryCompiler(private val configuration: Configuration,
                      private val fileManager: FileManager,
                      private val gson: Gson) {

    fun compile(pools: List<DevicePoolId>): Summary {
        val poolsSummary: List<PoolSummary> = pools.map { compilePoolSummary(it) }
        return Summary("Test", poolsSummary)
    }

    private fun compilePoolSummary(poolId: DevicePoolId): PoolSummary {
        val devices = fileManager.getFiles(FileType.DEVICE_INFO, poolId).map {
            readDeviceInfo(it)
        }
        val tests = devices.flatMap {
            readTests(poolId, it)
        }
        val passed = tests.count { it.status == TestStatus.PASSED }
        val ignored = tests.count { it.status == TestStatus.IGNORED }
        val failed = tests.count { it.status != TestStatus.PASSED && it.status != TestStatus.IGNORED }
        val duration = tests.sumByDouble { it.durationMillis() * 1.0 }.toLong()
        return PoolSummary(poolId = poolId,
                tests = tests,
                passed = passed,
                ignored = ignored,
                failed = failed,
                flaky = 0,
                durationMillis = duration,
                devices = devices)
    }

    private fun readTests(poolId: DevicePoolId, device: DeviceInfo): List<TestResult> {
        return fileManager.getTestResultFilesForDevice(poolId, device.serialNumber).map {
            gson.fromJson(FileReader(it), TestResult::class.java)
        }
    }

    private fun readDeviceInfo(file: File): DeviceInfo {
        return gson.fromJson(FileReader(file), DeviceInfo::class.java)
    }
}