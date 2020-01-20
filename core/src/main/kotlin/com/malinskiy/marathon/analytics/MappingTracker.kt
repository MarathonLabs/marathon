package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.internal.pub.Tracker
import com.malinskiy.marathon.analytics.internal.sub.CacheLoadEvent
import com.malinskiy.marathon.analytics.internal.sub.CacheStoreEvent
import com.malinskiy.marathon.analytics.internal.sub.DeviceConnectedEvent
import com.malinskiy.marathon.analytics.internal.sub.DevicePreparingEvent
import com.malinskiy.marathon.analytics.internal.sub.DeviceProviderPreparingEvent
import com.malinskiy.marathon.analytics.internal.sub.ExecutingBatchEvent
import com.malinskiy.marathon.analytics.internal.sub.InstallationCheckEvent
import com.malinskiy.marathon.analytics.internal.sub.InstallationEvent
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test
import java.time.Instant

class MappingTracker(private val trackerInternal: TrackerInternal) : Tracker {
    override fun deviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
        trackerInternal.track(DeviceConnectedEvent(Instant.now(), poolId, device))
    }

    override fun deviceProviderInit(serialNumber: String, startTime: Instant, finishTime: Instant) {
        trackerInternal.track(DeviceProviderPreparingEvent(startTime, finishTime, serialNumber))
    }

    override fun devicePreparing(serialNumber: String, startTime: Instant, finishTime: Instant) {
        trackerInternal.track(DevicePreparingEvent(startTime, finishTime, serialNumber))
    }

    override fun installationCheck(serialNumber: String, startTime: Instant, finishTime: Instant) {
        trackerInternal.track(InstallationCheckEvent(startTime, finishTime, serialNumber))
    }

    override fun installation(serialNumber: String, startTime: Instant, finishTime: Instant) {
        trackerInternal.track(InstallationEvent(startTime, finishTime, serialNumber))
    }

    override fun executingBatch(serialNumber: String, startTime: Instant, finishTime: Instant) {
        trackerInternal.track(ExecutingBatchEvent(startTime, finishTime, serialNumber))
    }

    override fun cacheStore(startTime: Instant, finishTime: Instant, test: Test) {
        trackerInternal.track(CacheStoreEvent(startTime, finishTime, test))
    }

    override fun cacheLoad(startTime: Instant, finishTime: Instant, test: Test) {
        trackerInternal.track(CacheLoadEvent(startTime, finishTime, test))
    }

    override fun test(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult, final: Boolean) {
        trackerInternal.track(TestEvent(Instant.now(), poolId, device, testResult, final))
    }
}
