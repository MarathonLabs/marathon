package com.malinskiy.marathon.test

import com.malinskiy.marathon.vendor.VendorConfiguration
import org.amshove.kluent.mock
import java.nio.file.Files

class Mocks {
    class TestParser {
        companion object {
            val DEFAULT = mock(com.malinskiy.marathon.execution.TestParser::class)
        }
    }

    class DeviceProvider {
        companion object {
            val DEFAULT = StubDeviceProvider()
        }
    }

    class Configuration {
        companion object {
            val DEFAULT = com.malinskiy.marathon.execution.Configuration(
                    name = "DEFAULT_TEST_CONFIG",
                    outputDir = Files.createTempDirectory("test-run").toFile(),
                    vendorConfiguration = object : VendorConfiguration {
                        override fun testParser(): com.malinskiy.marathon.execution.TestParser? = TestParser.DEFAULT
                        override fun deviceProvider(): com.malinskiy.marathon.device.DeviceProvider? = DeviceProvider.DEFAULT
                    },
                    debug = null,
                    batchingStrategy = null,
                    analyticsConfiguration = null,
                    excludeSerialRegexes = null,
                    fallbackToScreenshots = null,
                    filteringConfiguration = null,
                    flakinessStrategy = null,
                    ignoreFailures = null,
                    includeSerialRegexes = null,
                    isCodeCoverageEnabled = null,
                    poolingStrategy = null,
                    retryStrategy = null,
                    shardingStrategy = null,
                    sortingStrategy = null,
                    testClassRegexes = null,
                    testOutputTimeoutMillis = null
            )
        }
    }
}