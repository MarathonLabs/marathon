package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.cli.args.EnvironmentConfiguration
import com.malinskiy.marathon.cli.args.environment.EnvironmentReader
import com.malinskiy.marathon.cli.config.time.InstantTimeProvider
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.CompositionFilter
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.AbiPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ComboPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ManufacturerPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ModelPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.OperatingSystemVersionPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import com.malinskiy.marathon.ios.IOSConfiguration
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotThrow
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

object ConfigFactorySpec : Spek({
    given("ConfigFactory") {
        val referenceInstant = Instant.ofEpochSecond(1000000)
        val mockInstantTimeProvider = object : InstantTimeProvider {
            override fun referenceTime(): Instant = referenceInstant
        }

        lateinit var parser: ConfigFactory
        beforeEachTest {
            val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            mapper.registerModule(DeserializeModule(mockInstantTimeProvider))
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
            parser = ConfigFactory(mapper)
        }

        fun mockEnvironmentReader(path: String? = null): EnvironmentReader {
            val environmentReader: EnvironmentReader =  mock()
            whenever(environmentReader.read()) `it returns` EnvironmentConfiguration(path?.let { File(it) })
            return environmentReader
        }

        on("sample config 1") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_1.yaml").file)

            it("should deserialize") {
                val configuration = parser.create(file, mockEnvironmentReader())

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
                        FullyQualifiedClassnameFilter(".*".toRegex()),
                        TestMethodFilter(".*".toRegex()),
                        CompositionFilter(listOf(TestPackageFilter(".*".toRegex()),
                                TestMethodFilter(".*".toRegex())), CompositionFilter.OPERATION.UNION)
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
                configuration.testBatchTimeoutMillis shouldEqual 20_000
                configuration.testOutputTimeoutMillis shouldEqual 30_000
                configuration.debug shouldEqual true

                configuration.vendorConfiguration shouldEqual AndroidConfiguration(
                        File("/local/android"),
                        File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
                        File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
                        true,
                        true,
                        true,
                        30_000
                )
            }
        }
        on("sample config 2") {

            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_2.yaml").file)

            it("should deserialize with minimal configuration") {
                val configuration = parser.create(file, mockEnvironmentReader())

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
                configuration.testBatchTimeoutMillis shouldEqual 900_000
                configuration.testOutputTimeoutMillis shouldEqual 60_000
                configuration.debug shouldEqual true
                configuration.vendorConfiguration shouldEqual AndroidConfiguration(
                        File("/local/android"),
                        File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
                        File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
                        false,
                        false,
                        false,
                        30_000
                )
            }
        }

        on("config with ios vendor configuration") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_3.yaml").file)

            it("should initialize a specific vendor configuration") {
                val configuration = parser.create(file, mockEnvironmentReader())

                configuration.vendorConfiguration shouldEqual IOSConfiguration(
                        derivedDataDir = file.parentFile.resolve("a"),
                        xctestrunPath = file.parentFile.resolve("a/Build/Products/UITesting_iphonesimulator11.0-x86_64.xctestrun"),
                        remoteUsername = "testuser",
                        remotePrivateKey = File("/home/testuser/.ssh/id_rsa"),
                        knownHostsPath = file.parentFile.resolve("known_hosts"),
                        remoteRsyncPath = "/usr/local/bin/rsync",
                        debugSsh = true,
                        alwaysEraseSimulators = false,
                        hideRunnerOutput = true,
                        compactOutput = true,
                        keepAliveIntervalMillis = 300000L,
                        devicesFile = file.parentFile.resolve("Testdevices"))
            }
        }

        on("configuration without an explicit remote rsync path") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_4.yaml").file)

            it("should initialize a default one") {
                val configuration = parser.create(file, mockEnvironmentReader())

                val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
                iosConfiguration.remoteRsyncPath shouldEqual "/usr/bin/rsync"
            }
        }

        on("configuration without an explicit xctestrun path") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_5.yaml").file)

            it("should throw an exception") {
                val create = { parser.create(file, mockEnvironmentReader()) }

                create shouldThrow ConfigurationException::class
            }
        }

        on("configuration without androidSdk value") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_6.yaml").file)
            val environmentReader = mockEnvironmentReader("/android/home")

            it("should use value provided by environment") {
                val configuration = parser.create(file, environmentReader)

                configuration.vendorConfiguration shouldEqual AndroidConfiguration(
                        environmentReader.read().androidSdk!!,
                        File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
                        File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
                        false,
                        false,
                        false,
                        30_000
                )
            }
        }

        on("configuration without androidSdk value") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_7.yaml").file)

            it("should throw an exception when ANDROID_HOME is not set") {
                val create = { parser.create(file, mockEnvironmentReader(null)) }

                create shouldNotThrow ConfigurationException::class
            }
        }

        on("configuration with whitelist but no blacklist") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_8.yaml").file)

            it("should initialize an empty blacklist") {
                val configuration = parser.create(file, mockEnvironmentReader())

                configuration.filteringConfiguration.whitelist shouldEqual listOf(
                        SimpleClassnameFilter(".*".toRegex())
                )

                configuration.filteringConfiguration.blacklist shouldBe emptyList()
            }
        }

        on("configuration with blacklist but no whitelist") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_9.yaml").file)

            it("should initialize an empty whitelist") {
                val configuration = parser.create(file, mockEnvironmentReader())

                configuration.filteringConfiguration.whitelist shouldBe emptyList()

                configuration.filteringConfiguration.blacklist shouldEqual listOf(
                        SimpleClassnameFilter(".*".toRegex())
                )
            }
        }

        on("configuration time limits specified as Duration") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_10.yaml").file)

            it("should be used as relative values") {
                val configuration = parser.create(file, mockEnvironmentReader())

                configuration.sortingStrategy `should be instance of` ExecutionTimeSortingStrategy::class
                val sortingStrategy = configuration.sortingStrategy as ExecutionTimeSortingStrategy
                sortingStrategy.timeLimit shouldEqual referenceInstant.minus(Duration.ofHours(1))

                configuration.flakinessStrategy `should be instance of` ProbabilityBasedFlakinessStrategy::class
                val flakinessStrategy = configuration.flakinessStrategy as ProbabilityBasedFlakinessStrategy
                flakinessStrategy.timeLimit shouldEqual referenceInstant.minus(Duration.ofDays(30))
            }
        }
    }
})
