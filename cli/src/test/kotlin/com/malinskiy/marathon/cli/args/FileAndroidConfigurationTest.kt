package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File

class FileAndroidConfigurationTest {
    private val configuration = FileAndroidConfiguration(
        VendorType.DDMLIB,
        null,
        null,
        File.createTempFile("foo", "bar"),
        outputs = null,
        null,
        null,
        null,
        null,
        null,
        null,
        SerialStrategy.AUTOMATIC,
        ScreenRecordConfiguration(),
        15000L,
        AllureConfiguration(),
        TimeoutConfiguration(),
        FileSyncConfiguration(),
    )

    private val env: File = File.createTempFile("foo", "bar")
    private val sdk: File = File.createTempFile("android", "sdk")

    @Test
    fun `if androidSdk is null should throw Exception if env android sdk also is null`() {
        { configuration.toAndroidConfiguration(null) } shouldThrow ConfigurationException::class
    }

    @Test
    fun `if androidSdk is null should use env android sdk if it is not null`() {
        configuration.toAndroidConfiguration(env).androidSdk shouldBeEqualTo env
    }

    @Test
    fun `if android sdk is not null should use android sdk instead of env if both exists`() {
        configuration.copy(androidSdk = sdk).toAndroidConfiguration(env).androidSdk shouldBeEqualTo sdk
    }

    @Test
    fun `if android sdk is not null, test application output should be null by default`() {
        configuration.toAndroidConfiguration(env).applicationOutput shouldBeEqualTo null
    }

    @Test
    fun `if android sdk is not null, test application output should be null if provided`() {
        configuration.copy(applicationOutput = env).toAndroidConfiguration(env).applicationOutput shouldBeEqualTo env
    }

    @Test
    fun `if android sdk is not null test application apk should be equal`() {
        configuration.copy(testApplicationOutput = env).toAndroidConfiguration(env).testApplicationOutput shouldBeEqualTo env
    }

    @Test
    fun `if android sdk is not null auto grant permissions should be false by default`() {
        configuration.toAndroidConfiguration(env).autoGrantPermission shouldBeEqualTo false
    }

    @Test
    fun `if android sdk is not null auto grant permissions should be equal`() {
        configuration.copy(autoGrantPermission = false).toAndroidConfiguration(env).autoGrantPermission shouldBeEqualTo false
        configuration.copy(autoGrantPermission = true).toAndroidConfiguration(env).autoGrantPermission shouldBeEqualTo true
    }

    @Test
    fun `if android sdk is not null adb init timeout millis should be 30_000 by default`() {
        configuration.toAndroidConfiguration(env).adbInitTimeoutMillis shouldBeEqualTo 30_000
    }

    @Test
    fun `if android sdk is not null adb init timeout millis should be equal`() {
        val timeout = 500_000
        configuration.copy(adbInitTimeoutMillis = timeout).toAndroidConfiguration(env).adbInitTimeoutMillis shouldBeEqualTo timeout
    }

    @Test
    fun `if android sdk is not null install options should be empty string by default`() {
        configuration.toAndroidConfiguration(env).installOptions shouldBeEqualTo ""
    }

    @Test
    fun `if android sdk is not null install options should be equal if provided`() {
        configuration.copy(installOptions = "-d").toAndroidConfiguration(env).installOptions shouldBeEqualTo "-d"
    }
}
