package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DeviceActorTest {
    lateinit var job: Job
    lateinit var poolChannel: Channel<DevicePoolMessage>

    @BeforeEach
    fun setup() {
        poolChannel = Channel()
        job = Job()
    }

    @AfterEach
    fun teardown() {
        job.cancel()
    }

    @Test
    fun `terminate while in progress`() {
        val devicePoolId = DevicePoolId("test")
        val device = StubDevice(prepareTimeMillis = 1000L, testTimeMillis = 10000L)
        val actor = DeviceActor(
            devicePoolId, poolChannel, defaultConfiguration.copy(
                uncompletedTestRetryQuota = 0,
                batchingStrategy = BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(size = 1)
            ), device, job, Dispatchers.Unconfined
        )

        runBlocking {
            val test1 = generateTest()
            val testBatch = TestBatch(listOf(test1))
            device.executionResults = mapOf(test1 to Array(1) { TestStatus.FAILURE })

            actor.send(DeviceEvent.Initialize)
            var message = poolChannel.receive()
            message.shouldBeEqualTo(DevicePoolMessage.FromDevice.IsReady(device))

            actor.send(DeviceEvent.Execute(testBatch))
            actor.send(DeviceEvent.Terminate)

            message = poolChannel.receive()
            message.shouldBeEqualTo(DevicePoolMessage.FromDevice.ReturnTestBatch(device, testBatch, "Device serial-1 terminated"))
        }
    }

    private val defaultConfiguration = Configuration.Builder(
        name = "",
        outputDir = File(""),
    ).apply {
        vendorConfiguration = VendorConfiguration.StubVendorConfiguration
        analyticsTracking = false
    }.build()
}
