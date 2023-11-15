package com.malinskiy.marathon.config.vendor

import com.malinskiy.marathon.config.environment.EnvironmentConfiguration
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import org.mockito.kotlin.mock
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.whenever
import java.io.File

class AndroidConfigurationTest {
    private val mockMarathonFileDir = File("/some/folder/with/marathonfile/")
    private val env: File = File.createTempFile("foo", "bar")
    private val sdk: File = File.createTempFile("android", "sdk")

    @Test
    fun `if androidSdk is null should throw Exception if env android sdk also is null`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(null))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        assertThrows<ConfigurationException> {
            configurationFactory.parse(marathonfile)
        }
    }

    @Test
    fun `if androidSdk is null should use env android sdk if it is not null`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.androidSdk shouldBeEqualTo env
    }

    @Test
    fun `if android sdk is not null should use android sdk instead of env if both exists`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_2.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.androidSdk!!.absolutePath shouldBeEqualTo "/opt/android-sdk"
    }

    @Test
    fun `if android sdk is not null, test application output should be null by default`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.applicationOutput shouldBeEqualTo null
    }

    @Test
    fun `if android sdk is not null, test application output should be null if provided`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_3.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.applicationOutput shouldBeEqualTo File("/some/folder/with/marathonfile/debug.apk")
    }

    @Test
    fun `if android sdk is not null test application apk should be equal`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_2.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.testApplicationOutput shouldBeEqualTo File("/some/folder/with/marathonfile/foo/bar")
    }

    @Test
    fun `if android sdk is not null auto grant permissions should be false by default`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.autoGrantPermission shouldBeEqualTo false
    }

    @Test
    fun `if android sdk is not null auto grant permissions should be equal`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.autoGrantPermission shouldBeEqualTo false
    }

    @Test
    fun `if android sdk is not null adb init timeout millis should be 30_000 by default`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.adbInitTimeoutMillis shouldBeEqualTo 30_000
    }

    @Test
    fun `if android sdk is not null adb init timeout millis should be equal`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_4.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.adbInitTimeoutMillis shouldBeEqualTo 500_000
    }

    @Test
    fun `if android sdk is not null install options should be empty string by default`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.installOptions shouldBeEqualTo ""
    }

    @Test
    fun `if android sdk is not null install options should be equal if provided`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_5.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.installOptions shouldBeEqualTo "-d"
    }

    @Test
    fun `if android sdk is not null, extra applications output should be equal if provided`() {
        val marathonFile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_6.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonFile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.extraApplicationsOutput shouldBeEqualTo listOf(File("/some/folder/with/marathonfile/foo/bar"))
    }

    @Test
    fun `if android sdk is not null, extra applications output should be null by default`() {
        val marathonfile = File(AndroidConfigurationTest::class.java.getResource("/fixture/config/android/sample_1.yaml").file)
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(env))

        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            environmentReader = environmentReader,
        )

        val androidConfiguration = configurationFactory.parse(marathonfile).vendorConfiguration as VendorConfiguration.AndroidConfiguration
        androidConfiguration.extraApplicationsOutput shouldBeEqualTo null
    }
}
