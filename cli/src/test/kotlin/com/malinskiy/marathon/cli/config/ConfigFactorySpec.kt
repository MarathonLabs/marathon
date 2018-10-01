package com.malinskiy.marathon.cli.config

import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.execution.*
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.*
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import com.malinskiy.marathon.ios.IOSConfiguration
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

object ConfigFactorySpec : Spek({
    given("ConfigFactory") {
        val parser = ConfigFactory()

        on("sample config 1") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_1.yaml").file)

            it("should deserialize") {
                val configuration = parser.create(file, File("/local/android"), null)

                configuration.name shouldEqual "sample-app tests"
                configuration.outputDir shouldEqual File("./marathon")
                configuration.analyticsConfiguration shouldEqual AnalyticsConfiguration.InfluxDbConfiguration(
                        url = "http://influx.svc.cluster.local:8086",
                        user = "root",
                        password = "root",
                        dbName = "marathon",
                        retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
                )
                configuration.poolingStrategy shouldEqual ComboPoolingStrategy(
                        listOf(
                                OmniPoolingStrategy(),
                                ModelPoolingStrategy(),
                                OperatingSystemVersionPoolingStrategy(),
                                ManufacturerPoolingStrategy(),
                                AbiPoolingStrategy()
                        )
                )
                configuration.shardingStrategy shouldEqual CountShardingStrategy(5)
                configuration.sortingStrategy shouldEqual SuccessRateSortingStrategy(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2015-03-14T09:26:53.590Z")))
                configuration.batchingStrategy shouldEqual FixedSizeBatchingStrategy(5)
                configuration.flakinessStrategy shouldEqual ProbabilityBasedFlakinessStrategy(0.7, 3, Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2015-03-14T09:26:53.590Z")))
                configuration.retryStrategy shouldEqual FixedQuotaRetryStrategy(100, 2)
                SimpleClassnameFilter(".*".toRegex()) shouldEqual SimpleClassnameFilter(".*".toRegex())

                configuration.filteringConfiguration.whitelist shouldContainAll listOf(
                        SimpleClassnameFilter(".*".toRegex()),
                        FullyQualifiedClassnameFilter(".*".toRegex())
                )

                configuration.filteringConfiguration.blacklist shouldContainAll listOf(
                        TestPackageFilter(".*".toRegex()),
                        AnnotationFilter(".*".toRegex())
                )
                configuration.testClassRegexes.map { it.toString() } shouldContainAll listOf("^((?!Abstract).)*Test$")


                configuration.includeSerialRegexes shouldEqual emptyList()
                configuration.excludeSerialRegexes shouldEqual emptyList()
                configuration.ignoreFailures shouldEqual false
                configuration.isCodeCoverageEnabled shouldEqual false
                configuration.fallbackToScreenshots shouldEqual false
                configuration.testOutputTimeoutMillis shouldEqual 30000
                configuration.debug shouldEqual true

                configuration.vendorConfiguration shouldEqual AndroidConfiguration(
                        File("/local/android"),
                        File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
                        File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
                        true,
                        30_000
                )
            }
        }
        on("sample config 2") {

            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_2.yaml").file)

            it("should deserialize with minimal configuration") {
                val configuration = parser.create(file, File("/local/android"), null)

                configuration.name shouldEqual "sample-app tests"
                configuration.outputDir shouldEqual File("./marathon")
                configuration.analyticsConfiguration shouldEqual AnalyticsConfiguration.DisabledAnalytics
                configuration.poolingStrategy shouldEqual OmniPoolingStrategy()
                configuration.shardingStrategy shouldEqual ParallelShardingStrategy()
                configuration.sortingStrategy shouldEqual NoSortingStrategy()
                configuration.batchingStrategy shouldEqual IsolateBatchingStrategy()
                configuration.flakinessStrategy shouldEqual IgnoreFlakinessStrategy()
                configuration.retryStrategy shouldEqual NoRetryStrategy()
                SimpleClassnameFilter(".*".toRegex()) shouldEqual SimpleClassnameFilter(".*".toRegex())

                configuration.filteringConfiguration.whitelist.shouldBeEmpty()
                configuration.filteringConfiguration.blacklist.shouldBeEmpty()

                configuration.testClassRegexes.map { it.toString() } shouldContainAll listOf("^((?!Abstract).)*Test$")

                configuration.includeSerialRegexes shouldEqual emptyList()
                configuration.excludeSerialRegexes shouldEqual emptyList()
                configuration.ignoreFailures shouldEqual false
                configuration.isCodeCoverageEnabled shouldEqual false
                configuration.fallbackToScreenshots shouldEqual false
                configuration.testOutputTimeoutMillis shouldEqual 60000
                configuration.debug shouldEqual true
                configuration.vendorConfiguration shouldEqual AndroidConfiguration(
                        File("/local/android"),
                        File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
                        File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
                        false,
                        30_000
                )
            }
        }

        on("config with ios vendor configuration") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_3.yaml").file)

            it("should initialize a specific vendor configuration") {
                val configuration = parser.create(file, null, null)

                configuration.vendorConfiguration shouldEqual IOSConfiguration(
                        derivedDataDir = file.parentFile.resolve("a"),
                        xctestrunPath = file.parentFile.resolve("a/Build/Products/UITesting_iphonesimulator11.0-x86_64.xctestrun"),
                        remoteUsername = "testuser",
                        remotePrivateKey = File("/home/testuser/.ssh/id_rsa"))
            }
        }

        on("config with xctestrun path overridden") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_4.yaml").file)

            it("should initialize a specific vendor configuration") {
                val configuration = parser.create(file, null, File("build.xctestrun"))

                configuration.vendorConfiguration shouldEqual IOSConfiguration(
                        derivedDataDir = file.parentFile.resolve("a"),
                        xctestrunPath = File("build.xctestrun"),
                        remoteUsername = "testuser",
                        remotePrivateKey = File("/home/testuser/.ssh/id_rsa")
                )
            }
        }

        on("configuration without an explicit xctestrun path") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_5.yaml").file)

            it("should throw an exception") {
                val create = { parser.create(file, null, null) }

                create shouldThrow ConfigurationException::class
            }
        }
    }
})
