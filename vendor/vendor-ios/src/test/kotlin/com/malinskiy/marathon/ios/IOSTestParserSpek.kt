package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldContainSame
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

object IOSTestParserSpek : Spek(
    {
        describe("iOS test parser") {
            val parser = IOSTestParser()

            on("project sources") {
                val sourceRoot =
                    File(javaClass.classLoader.getResource("sample-xcworkspace/sample-appUITests").file)
                val derivedDataDir =
                    File(javaClass.classLoader.getResource("sample-xcworkspace/derived-data").file)
                val xctestrunPath =
                    File(javaClass.classLoader.getResource("sample-xcworkspace/derived-data/Build/Products/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
                val configuration = Configuration(
                    name = "",
                    outputDir = File(""),
                    customAnalyticsTracker = null,
                    analyticsConfiguration = null,
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
                    isCodeCoverageEnabled = null,
                    fallbackToScreenshots = null,
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    vendorConfiguration = IOSConfiguration(
                        derivedDataDir = derivedDataDir,
                        xctestrunPath = xctestrunPath,
                        remoteUsername = "testuser",
                        remotePrivateKey = File("/home/fakekey"),
                        knownHostsPath = null,
                        remoteRsyncPath = "/remote/rsync",
                        sourceRoot = sourceRoot,
                        debugSsh = false,
                        alwaysEraseSimulators = true
                    ),
                    analyticsTracking = false
                )
                val iosComponentInfo = IOSComponentInfoExtractor().extract(configuration)

                it("should return accurate list of tests") {
                    val extractedTests = runBlocking { parser.extract(iosComponentInfo) }

                    extractedTests shouldContainSame listOf(
                        Test("sample-appUITests", "StoryboardTests", "testButton", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "StoryboardTests", "testLabel", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "MoreTests", "testPresentModal", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "CrashingTests", "testButton", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FailingTests", "testAlwaysFailing", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky1", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky2", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky3", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky4", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky5", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky6", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky7", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "FlakyTests", "testTextFlaky8", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "SlowTests", "testTextSlow", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "SlowTests", "testTextSlow1", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "SlowTests", "testTextSlow2", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "SlowTests", "testTextSlow3", emptyList(), iosComponentInfo),
                        Test("sample-appUITests", "SlowTests", "testTextSlow4", emptyList(), iosComponentInfo)
                    )
                }
            }
        }
    })
