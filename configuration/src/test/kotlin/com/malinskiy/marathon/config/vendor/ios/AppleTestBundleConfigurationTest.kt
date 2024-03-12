package com.malinskiy.marathon.config.vendor.ios

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.IOSConfigurationTest
import com.malinskiy.marathon.config.vendor.apple.AppleTestBundleConfiguration
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class AppleTestBundleConfigurationTest {
    
    private val tempDirFor: (File) -> File = {
        createTempDirectory(it.name).toFile().apply { deleteOnExit() }
    }
    
    @Test
    fun testExplicitConfig() {
        val app = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/derivedDataDir/sample.app").file)
        val testApp = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/derivedDataDir/ui.xctest").file)
        AppleTestBundleConfiguration(app, testApp, tempDirFor = tempDirFor).validate()
    }

    @Test
    fun testDerivedDir() {
        val derivedDir = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/derivedDataDir").file)
        AppleTestBundleConfiguration(derivedDataDir = derivedDir, tempDirFor = tempDirFor).validate()
    }
    
    @Test
    fun testIpa() {
        val app = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/ipa/app.ipa").file)
        val testApp = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/ipa/testApp.ipa").file)
        AppleTestBundleConfiguration(app, testApp, tempDirFor = tempDirFor).validate()
    }

    @Test
    fun testZip() {
        val app = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/zip/app.zip").file)
        val testApp = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/zip/testApp.zip").file)
        AppleTestBundleConfiguration(app, testApp, tempDirFor = tempDirFor).validate()
    }
    
    @Test
    fun testMissing() {
        invoking {
            AppleTestBundleConfiguration().validate()
        } shouldThrow ConfigurationException::class
    }
}
