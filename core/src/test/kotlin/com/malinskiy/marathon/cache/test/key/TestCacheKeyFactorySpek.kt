package com.malinskiy.marathon.cache.test.key

import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubComponentCacheKeyProvider
import com.malinskiy.marathon.test.StubComponentInfoExtractor
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestComponentInfo
import com.malinskiy.marathon.test.TestVendorConfiguration
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

class TestCacheKeyFactorySpek : Spek(
    {
        describe("TestCacheKeyFactory") {
            it("should return different cache keys for different marathon version") {
                runBlocking {
                    val firstKey = createCacheKey(marathonVersion = "1.0")
                    val secondKey = createCacheKey(marathonVersion = "1.1")

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same marathon version") {
                runBlocking {
                    val firstKey = createCacheKey(marathonVersion = "1.0")
                    val secondKey = createCacheKey(marathonVersion = "1.0")

                    firstKey shouldEqual secondKey
                }
            }

            it("should return different cache keys for different component cache keys") {
                runBlocking {
                    val firstKey = createCacheKey(componentCacheKey = "abc")
                    val secondKey = createCacheKey(componentCacheKey = "def")

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same component cache keys") {
                runBlocking {
                    val firstKey = createCacheKey(componentCacheKey = "abc")
                    val secondKey = createCacheKey(componentCacheKey = "abc")

                    firstKey shouldEqual secondKey
                }
            }

            it("should return different cache keys for different device pools") {
                runBlocking {
                    val firstKey = createCacheKey(devicePoolId = DevicePoolId("abc"))
                    val secondKey = createCacheKey(devicePoolId = DevicePoolId("def"))

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same device pools") {
                runBlocking {
                    val firstKey = createCacheKey(devicePoolId = DevicePoolId("abc"))
                    val secondKey = createCacheKey(devicePoolId = DevicePoolId("abc"))

                    firstKey shouldEqual secondKey
                }
            }

            it("should return different cache keys for different code coverage configurations") {
                runBlocking {
                    val firstKey = createCacheKey(configuration = createConfiguration(codeCoverageEnabled = true))
                    val secondKey = createCacheKey(configuration = createConfiguration(codeCoverageEnabled = false))

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same code coverage configurations") {
                runBlocking {
                    val firstKey = createCacheKey(configuration = createConfiguration(codeCoverageEnabled = true))
                    val secondKey = createCacheKey(configuration = createConfiguration(codeCoverageEnabled = true))

                    firstKey shouldEqual secondKey
                }
            }

            it("should return different cache keys for different test package names") {
                runBlocking {
                    val firstKey = createCacheKey(test = createTest(packageName = "abc"))
                    val secondKey = createCacheKey(test = createTest(packageName = "def"))

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same package names") {
                runBlocking {
                    val firstKey = createCacheKey(test = createTest(packageName = "abc"))
                    val secondKey = createCacheKey(test = createTest(packageName = "abc"))

                    firstKey shouldEqual secondKey
                }
            }

            it("should return different cache keys for different class names") {
                runBlocking {
                    val firstKey = createCacheKey(test = createTest(clazz = "abc"))
                    val secondKey = createCacheKey(test = createTest(clazz = "def"))

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same class names") {
                runBlocking {
                    val firstKey = createCacheKey(test = createTest(clazz = "abc"))
                    val secondKey = createCacheKey(test = createTest(clazz = "abc"))

                    firstKey shouldEqual secondKey
                }
            }

            it("should return different cache keys for different method names") {
                runBlocking {
                    val firstKey = createCacheKey(test = createTest(method = "abc"))
                    val secondKey = createCacheKey(test = createTest(method = "def"))

                    firstKey shouldNotEqual secondKey
                }
            }

            it("should return same cache keys for the same method names") {
                runBlocking {
                    val firstKey = createCacheKey(test = createTest(method = "abc"))
                    val secondKey = createCacheKey(test = createTest(method = "abc"))

                    firstKey shouldEqual secondKey
                }
            }
        }
    })

private fun createCacheKey(
    marathonVersion: String = "123",
    componentCacheKey: String = "abc",
    configuration: Configuration = createConfiguration(),
    devicePoolId: DevicePoolId = DevicePoolId("omni"),
    test: Test = createTest()
): CacheKey = runBlocking {
    val componentCacheKeyProvider = object : ComponentCacheKeyProvider {
        override suspend fun getCacheKey(componentInfo: ComponentInfo): String = componentCacheKey
    }
    val versionNameProvider = mock<VersionNameProvider> {
        on { this.versionName }.thenReturn(marathonVersion)
    }
    val cacheKeyFactory = TestCacheKeyFactory(componentCacheKeyProvider, versionNameProvider, configuration)
    cacheKeyFactory.getCacheKey(devicePoolId, test)
}

private fun createTest(
    packageName: String = "com.test",
    clazz: String = "Test",
    method: String = "test1"
) = Test(
    pkg = packageName,
    clazz = clazz,
    method = method,
    componentInfo = TestComponentInfo(someInfo = "someInfo", name = "component-name"),
    metaProperties = emptyList()
)

private fun createConfiguration(codeCoverageEnabled: Boolean = false) = Configuration(
    name = "",
    outputDir = File(""),
    analyticsConfiguration = null,
    customAnalyticsTracker = null,
    poolingStrategy = null,
    shardingStrategy = null,
    sortingStrategy = null,
    batchingStrategy = null,
    flakinessStrategy = null,
    retryStrategy = null,
    filteringConfiguration = null,
    strictRunFilterConfiguration = null,
    cache = null,
    ignoreFailures = null,
    isCodeCoverageEnabled = codeCoverageEnabled,
    fallbackToScreenshots = null,
    strictMode = null,
    uncompletedTestRetryQuota = null,
    testClassRegexes = null,
    includeSerialRegexes = null,
    excludeSerialRegexes = null,
    testBatchTimeoutMillis = null,
    testOutputTimeoutMillis = null,
    debug = false,
    vendorConfiguration = TestVendorConfiguration(
        Mocks.TestParser.DEFAULT,
        StubDeviceProvider(),
        StubComponentInfoExtractor(),
        StubComponentCacheKeyProvider()
    ),
    analyticsTracking = false
)