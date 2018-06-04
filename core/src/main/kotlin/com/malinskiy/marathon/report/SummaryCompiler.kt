package com.malinskiy.marathon.report

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.report.internal.DeviceInfoReporter
import com.malinskiy.marathon.report.internal.TestResultSerializer

class SummaryCompiler(private val deviceInfoSerializer: DeviceInfoReporter,
                      private val testResultSerializer: TestResultSerializer,
                      private val configuration: Configuration) {

    fun compile(pools: List<DevicePoolId>): Summary {
        val poolsSummary: List<PoolSummary> = pools.map { compilePoolSummary(it) }
        return Summary(configuration.name, poolsSummary)
    }

    private fun compilePoolSummary(poolId: DevicePoolId): PoolSummary {
        val devices = deviceInfoSerializer.getDevices(poolId)
        val tests = devices.flatMap {
            testResultSerializer.readTests(poolId, it)
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
}
