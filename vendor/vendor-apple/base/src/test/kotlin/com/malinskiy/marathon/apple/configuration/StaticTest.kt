package com.malinskiy.marathon.apple.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.malinskiy.marathon.config.vendor.apple.SshAuthentication
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class StaticTest {
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

    @Test
    fun testSample1() {
        val src = File(StaticTest::class.java.getResource("/fixtures/marathondevices/sample_1.yaml").file)
        val actual = mapper.readValue<Marathondevices>(src)

        actual shouldBeEqualTo Marathondevices(
            workers = listOf(
                Worker(
                    transport = Transport.Local,
                    devices = listOf(
                        AppleTarget.Simulator("XXX"),
                        AppleTarget.Physical("YYY"),
                    ) 
                ),
                Worker(
                    transport = Transport.Ssh(
                        addr = "node-1.device-farm.example.com",
                        port = 44,
                        authentication = SshAuthentication.PasswordAuthentication(
                            username = "vasya",
                            password = "pupking",
                        )
                    ),
                    devices = listOf(
                        AppleTarget.SimulatorProfile(
                            deviceTypeId = "com.apple.CoreSimulator.SimDeviceType.iPhone-X",
                            runtimeId = "com.apple.CoreSimulator.SimRuntime.iOS-16-2",
                            newNamePrefix = "prefix",
                        )
                    )
                )
            )
        )
    }
}
