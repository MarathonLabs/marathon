package com.malinskiy.marathon.config.environment

import com.malinskiy.marathon.cli.args.environmentConfiguration
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class SystemEnvironmentReaderTest {

    private lateinit var environment: (String) -> String?
    private lateinit var systemEnvironmentReader: SystemEnvironmentReader

    private val expectedAndroidSdk = "/android"

    @BeforeEach
    fun beforeEach() {
        environment = mock()
        systemEnvironmentReader = SystemEnvironmentReader(environment)
    }

    @Test
    fun `EnvironmentConfiguration should point to ANDROID_HOME`() {
        givenEnvironment("ANDROID_HOME")

        val environmentConfiguration = systemEnvironmentReader.read()

        environmentConfiguration shouldBeEqualTo expectedAndroidSdk()
    }

    @Test
    fun `EnvironmentConfiguration should point to ANDROID_SDK_ROOT`() {
        givenEnvironment("ANDROID_SDK_ROOT")

        val environmentConfiguration = systemEnvironmentReader.read()

        environmentConfiguration shouldBeEqualTo expectedAndroidSdk()
    }

    @Test
    fun `EnvironmentConfiguration should prefer ANDROID_HOME over ANDROID_SDK_ROOT`() {
        givenEnvironment("ANDROID_HOME")
        givenEnvironment("ANDROID_SDK_ROOT", "/android_sdk")

        val environmentConfiguration = systemEnvironmentReader.read()

        environmentConfiguration shouldBeEqualTo expectedAndroidSdk()
    }

    private fun expectedAndroidSdk(): EnvironmentConfiguration = environmentConfiguration(androidSdk = File(expectedAndroidSdk))

    private fun givenEnvironment(name: String, value: String = expectedAndroidSdk) {
        whenever(environment.invoke(name)).thenReturn(value)
    }
}
