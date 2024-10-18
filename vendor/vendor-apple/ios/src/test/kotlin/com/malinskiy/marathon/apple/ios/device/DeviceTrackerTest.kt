package com.malinskiy.marathon.apple.ios.device

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.malinskiy.marathon.apple.configuration.AppleTarget
import com.malinskiy.marathon.apple.configuration.Marathondevices
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DeviceTrackerTest {
    @Test
    fun testWorkerSimple() {

        val marathondevices = readFixture("all.yaml")
        val worker = marathondevices.workers.first()
        val workerTracker = WorkerTracker(worker.transport)
        val update: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(worker.devices)

        val simulator = AppleTarget.Simulator("12345-ABCDE-54321")
        val simulatorProfile = AppleTarget.SimulatorProfile(
            deviceTypeId = "iPhone X", runtimeId = "com.apple.CoreSimulator.SimRuntime.iOS-14-5", newNamePrefix = "testSim"
        )
        val physical = AppleTarget.Physical("98765-ZYXWV-56789")
        val actual = update.map { it.second }.toList()


        assertThat(actual).containsExactlyInAnyOrder(
            TrackingUpdate.Connected(AppleTarget.Host),
            TrackingUpdate.Connected(simulator),
            TrackingUpdate.Connected(simulatorProfile),
            TrackingUpdate.Connected(physical),
        )

        //Same state
        val noopUpdate: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(worker.devices)
        val actualNoop = noopUpdate.map { it.second }.toList()
        assertThat(actualNoop).isEmpty()

        //All x2
        val marathondevicesX2 = readFixture("allx2.yaml")
        val workerX2 = marathondevicesX2.workers.first()
        val simulatorx2 = AppleTarget.Simulator("12345-ABCDE-54322")
        val simulatorProfilex2 = AppleTarget.SimulatorProfile(
            deviceTypeId = "iPhone X", runtimeId = "com.apple.CoreSimulator.SimRuntime.iOS-14-5", newNamePrefix = "testSim"
        )
        val physicalx2 = AppleTarget.Physical("98765-ZYXWV-56780")
        val x2Update: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(workerX2.devices)
        val actualX2 = x2Update.map { it.second }.toList()

        assertThat(actualX2).containsExactlyInAnyOrder(
            TrackingUpdate.Connected(simulatorx2),
            TrackingUpdate.Connected(simulatorProfilex2),
            TrackingUpdate.Connected(physicalx2),
        )
    }

    @Test
    fun testSimulatorProfileScaling() {
        val marathondevices = readFixture("simulatorprofile.yaml")
        val worker = marathondevices.workers.first()
        val workerTracker = WorkerTracker(worker.transport)
        val update: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(worker.devices)
        val actual1 = update.map { it.second }.toList()

        val simulatorProfile = AppleTarget.SimulatorProfile(
            deviceTypeId = "iPhone X", runtimeId = "com.apple.CoreSimulator.SimRuntime.iOS-14-5", newNamePrefix = "testSim"
        )

        assertThat(actual1).containsExactlyInAnyOrder(
            TrackingUpdate.Connected(simulatorProfile),
        )

        //Scale up x3
        val marathondevicesX3 = readFixture("simulatorprofilex3.yaml")
        val workerX3 = marathondevicesX3.workers.first()
        val x3Update: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(workerX3.devices)
        val actualX3 = x3Update.map { it.second }.toList()

        assertThat(actualX3).containsExactlyInAnyOrder(
            TrackingUpdate.Connected(simulatorProfile),
            TrackingUpdate.Connected(simulatorProfile),
        )

        //Scale down to x2
        val marathondevicesX2 = readFixture("simulatorprofilex2.yaml")
        val workerX2 = marathondevicesX2.workers.first()
        val x2Update: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(workerX2.devices)
        val actualX2 = x2Update.map { it.second }.toList()

        assertThat(actualX2).isEmpty()

        //Scale up to x4
        val marathondevicesX4 = readFixture("simulatorprofilex4.yaml")
        val workerX4 = marathondevicesX4.workers.first()
        val x4Update: List<Pair<AppleTarget, TrackingUpdate>> = workerTracker.update(workerX4.devices)
        val actualX4 = x4Update.map { it.second }.toList()

        assertThat(actualX4).containsExactlyInAnyOrder(
            TrackingUpdate.Connected(simulatorProfile),
        )
    }

    fun readFixture(path: String): Marathondevices {
        val src = File(DeviceTrackerTest::class.java.getResource("/fixtures/marathondevices/$path").file)
        return mapper.readValue<Marathondevices>(src)
    }

    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun `setup yaml mapper`() {
        mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
        mapper.registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, true)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
    }
}
