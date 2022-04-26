package com.malinskiy.marathon.android

import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.test.MetaProperty
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class AndroidTestParserTest {
    private val testBundleIdentifier = AndroidTestBundleIdentifier()
    private val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
    private val vendorConfiguration = VendorConfiguration.AndroidConfiguration(
        androidSdk = File(""),
        applicationOutput = File(""),
        testApplicationOutput = apkFile,
        extraApplicationsOutput = emptyList()
    )
    private val parser = LocalTestParser(vendorConfiguration, testBundleIdentifier)

    @Test
    fun `should return proper list of test methods`() {
        val extractedTests = runBlocking {
            parser.extract()
        }
        extractedTests shouldBeEqualTo listOf(
            MarathonTest(
                "com.example", "MainActivityTest", "testText",
                listOf(
                    MetaProperty("org.junit.Test"),
                    MetaProperty("kotlin.Metadata"),
                    MetaProperty("org.junit.runner.RunWith")
                )
            )
        )
    }
}
