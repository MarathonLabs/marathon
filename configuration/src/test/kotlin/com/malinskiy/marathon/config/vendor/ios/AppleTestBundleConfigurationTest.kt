package com.malinskiy.marathon.config.vendor.ios

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.IOSConfigurationTest
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
        val app = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle/sample.app").file)
        val testApp = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle/sample.xctest").file)
        AppleTestBundleConfiguration(app, testApp, tempDirFor = tempDirFor).validate()
    }

    @Test
    fun testDerivedDir() {
        val derivedDir = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle").file)
        AppleTestBundleConfiguration(derivedDataDir = derivedDir, tempDirFor = tempDirFor).validate()
    }
    
    @Test
    fun testIpa() {
        val app = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle/app.ipa").file)
        val testApp = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle/testApp.ipa").file)
        AppleTestBundleConfiguration(app, testApp, tempDirFor = tempDirFor).validate()
    }

    @Test
    fun testZip() {
        val app = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle/app.zip").file)
        val testApp = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/bundle/testApp.zip").file)
        AppleTestBundleConfiguration(app, testApp, tempDirFor = tempDirFor).validate()
    }
    
    @Test
    fun testMissing() {
        invoking {
            AppleTestBundleConfiguration().validate()
        } shouldThrow ConfigurationException::class
    }
}
