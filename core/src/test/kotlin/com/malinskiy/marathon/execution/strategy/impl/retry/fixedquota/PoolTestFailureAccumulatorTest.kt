package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class PoolTestFailureAccumulatorTest {
    private val accumulator = PoolTestFailureAccumulator()

    @Test
    fun `same device pool id, default retry count value = 0`() {
        val devicePoolId = DevicePoolId("DevicePoolId")
        val test = generateTest()

        accumulator.getCount(devicePoolId, test) shouldBe 0
    }

    @Test
    fun `same device pool id, record should increment retry counter`() {
        val devicePoolId = DevicePoolId("DevicePoolId")
        val test = generateTest()

        accumulator.getCount(devicePoolId, test) shouldBe 0
        accumulator.record(devicePoolId, test)
        accumulator.getCount(devicePoolId, test) shouldBe 1
        accumulator.record(devicePoolId, test)
        accumulator.getCount(devicePoolId, test) shouldBe 2
    }

    @Test
    fun `same device pool id, record should increment only associated counter`() {
        val devicePoolId = DevicePoolId("DevicePoolId")
        val test = generateTest()

        val test2 = generateTest(method = "testMethod2")
        accumulator.getCount(devicePoolId, test) shouldBe 0
        accumulator.getCount(devicePoolId, test2) shouldBe 0
        accumulator.record(devicePoolId, test)
        accumulator.getCount(devicePoolId, test) shouldBe 1
        accumulator.getCount(devicePoolId, test2) shouldBe 0
        accumulator.record(devicePoolId, test2)
        accumulator.getCount(devicePoolId, test) shouldBe 1
        accumulator.getCount(devicePoolId, test2) shouldBe 1
        accumulator.record(devicePoolId, test2)
        accumulator.getCount(devicePoolId, test) shouldBe 1
        accumulator.getCount(devicePoolId, test2) shouldBe 2
    }

    @Test
    fun `different device pool ids record should increment counter only for specified pool id`() {
        val pool1 = DevicePoolId("DevicePoolId-1")
        val pool2 = DevicePoolId("DevicePoolId-2")
        val test = generateTest()
        accumulator.getCount(pool1, test) shouldBe 0
        accumulator.getCount(pool2, test) shouldBe 0
        accumulator.record(pool1, test)
        accumulator.record(pool2, test)
        accumulator.getCount(pool1, test) shouldBe 1
        accumulator.getCount(pool2, test) shouldBe 1
    }
}
