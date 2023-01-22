package com.malinskiy.marathon.config.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.config.environment.EnvironmentConfiguration
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.time.InstantTimeProvider
import com.malinskiy.marathon.config.serialization.yaml.SerializeModule
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.android.RecorderType
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenshotConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import com.malinskiy.marathon.config.vendor.ios.AppleTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.ios.LifecycleConfiguration
import com.malinskiy.marathon.config.vendor.ios.RsyncConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication
import com.malinskiy.marathon.config.vendor.ios.SshConfiguration
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

class ConfigurationFactoryTest {
    val mockMarathonFileDir = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config").file).parentFile

    val referenceInstant: Instant = Instant.ofEpochSecond(1000000)
    private val mockInstantTimeProvider = object : InstantTimeProvider {
        override fun referenceTime(): Instant = referenceInstant
    }

    lateinit var parser: ConfigurationFactory
    lateinit var mapper: ObjectMapper

    fun mockEnvironmentReader(path: String? = null): EnvironmentReader {
        val environmentReader = mock<EnvironmentReader>()
        whenever(environmentReader.read()).thenReturn(EnvironmentConfiguration(path?.let { File(it) }))
        return environmentReader
    }

    @BeforeEach
    fun `setup yaml mapper`() {
        mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
        mapper.registerModule(
            SerializeModule(
                mockInstantTimeProvider,
                mockMarathonFileDir,
            )
        )
            .registerModule(KotlinModule.Builder()
                                .withReflectionCacheSize(512)
                                .configure(KotlinFeature.NullToEmptyCollection, false)
                                .configure(KotlinFeature.NullToEmptyMap, false)
                                .configure(KotlinFeature.NullIsSameAsDefault, false)
                                .configure(KotlinFeature.SingletonSupport, true)
                                .configure(KotlinFeature.StrictNullChecks, false)
                                .build())
            .registerModule(JavaTimeModule())
        parser = ConfigurationFactory(marathonfileDir = mockMarathonFileDir, mapper = mapper)
    }

    @Test
    fun `on sample config 1 should deserialize`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_1.yaml").file)
        val configuration = parser.parse(file)

        configuration.name shouldBeEqualTo "sample-app tests"
        configuration.outputDir shouldBeEqualTo File(mockMarathonFileDir, "./marathon").canonicalFile
        configuration.analyticsConfiguration shouldBeEqualTo AnalyticsConfiguration.InfluxDbConfiguration(
            url = "http://influx.svc.cluster.local:8086",
            user = "root",
            password = "root",
            dbName = "marathon",
            retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
        )
        val poolingStrategyConfiguration = configuration.poolingStrategy as PoolingStrategyConfiguration.ComboPoolingStrategyConfiguration
        poolingStrategyConfiguration.list[0] shouldBeInstanceOf PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration::class
        poolingStrategyConfiguration.list[1] shouldBeInstanceOf PoolingStrategyConfiguration.ModelPoolingStrategyConfiguration::class
        poolingStrategyConfiguration.list[2] shouldBeInstanceOf PoolingStrategyConfiguration.OperatingSystemVersionPoolingStrategyConfiguration::class
        poolingStrategyConfiguration.list[3] shouldBeInstanceOf PoolingStrategyConfiguration.ManufacturerPoolingStrategyConfiguration::class
        poolingStrategyConfiguration.list[4] shouldBeInstanceOf PoolingStrategyConfiguration.AbiPoolingStrategyConfiguration::class

        configuration.shardingStrategy shouldBeEqualTo ShardingStrategyConfiguration.CountShardingStrategyConfiguration(5)
        configuration.sortingStrategy shouldBeEqualTo SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration(
            Instant.from(
                DateTimeFormatter.ISO_DATE_TIME.parse("2015-03-14T09:26:53.590Z")
            ), false
        )
        configuration.batchingStrategy shouldBeEqualTo BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(5)
        configuration.flakinessStrategy shouldBeEqualTo FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration(
            0.7,
            3,
            Instant.from(
                DateTimeFormatter.ISO_DATE_TIME.parse(
                    "2015-03-14T09:26:53.590Z"
                )
            )
        )
        configuration.retryStrategy shouldBeEqualTo
            RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration(100, 2)

        TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*".toRegex()) shouldBeEqualTo
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*".toRegex())

        configuration.filteringConfiguration.allowlist shouldContainSame listOf(
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*".toRegex()),
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(values = listOf("SimpleTest")),
            TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(".*".toRegex()),
            TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(
                file = File(
                    mockMarathonFileDir,
                    "filterfile"
                ).canonicalFile
            ),
            TestFilterConfiguration.TestMethodFilterConfiguration(".*".toRegex()),
            TestFilterConfiguration.CompositionFilterConfiguration(
                listOf(
                    TestFilterConfiguration.TestPackageFilterConfiguration(".*".toRegex()),
                    TestFilterConfiguration.TestMethodFilterConfiguration(".*".toRegex())
                ), TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.UNION
            )
        )

        configuration.filteringConfiguration.blocklist shouldContainSame listOf(
            TestFilterConfiguration.TestPackageFilterConfiguration(".*".toRegex()),
            TestFilterConfiguration.AnnotationFilterConfiguration(".*".toRegex()),
            TestFilterConfiguration.AnnotationDataFilterConfiguration(".*".toRegex(), ".*".toRegex())
        )
        configuration.testClassRegexes.map { it.toString() } shouldContainSame listOf("^((?!Abstract).)*Test$")

        // Regex doesn't have proper equals method. Need to check the patter itself
        configuration.includeSerialRegexes.joinToString(separator = "") { it.pattern } shouldBeEqualTo """emulator-500[2,4]""".toRegex().pattern
        configuration.excludeSerialRegexes.joinToString(separator = "") { it.pattern } shouldBeEqualTo """emulator-5002""".toRegex().pattern
        configuration.ignoreFailures shouldBeEqualTo false
        configuration.isCodeCoverageEnabled shouldBeEqualTo false
        configuration.strictMode shouldBeEqualTo true
        configuration.testBatchTimeoutMillis shouldBeEqualTo 20_000
        configuration.testOutputTimeoutMillis shouldBeEqualTo 30_000
        configuration.debug shouldBeEqualTo true
        configuration.screenRecordingPolicy shouldBeEqualTo ScreenRecordingPolicy.ON_ANY

        configuration.deviceInitializationTimeoutMillis shouldBeEqualTo 300_000
        configuration.vendorConfiguration shouldBeEqualTo VendorConfiguration.AndroidConfiguration(
            VendorConfiguration.AndroidConfiguration.VendorType.ADAM,
            File("/local/android"),
            File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
            File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
            listOf(
                File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-split-debug.apk"),
            ),
            null,
            null,
            true,
            mapOf("debug" to "false"),
            true,
            true,
            30_000,
            "-d",
            SerialStrategy.AUTOMATIC,
            ScreenRecordConfiguration(
                preferableRecorderType = RecorderType.SCREENSHOT,
                videoConfiguration = VideoConfiguration(false, 1080, 1920, 2, 300),
                screenshotConfiguration = ScreenshotConfiguration(false, 1080, 1920, 200)
            ),
            15000L,
            AllureConfiguration()
        )
    }

    @Test
    fun `on sample config 1 with custom retention policy should deserialize`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_1_rp.yaml").file)

        val configuration = parser.parse(file)
        configuration.analyticsConfiguration shouldBeEqualTo AnalyticsConfiguration.InfluxDbConfiguration(
            url = "http://influx.svc.cluster.local:8086",
            user = "root",
            password = "root",
            dbName = "marathon",
            retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration(
                "rpMarathonTest",
                "90d",
                "1h",
                5,
                false
            )
        )
    }

    @Test
    fun `on sample config 2 with minimal configuration should deserialize`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_2.yaml").file)
        val configuration = parser.parse(file)

        configuration.name shouldBeEqualTo "sample-app tests"
        configuration.outputDir shouldBeEqualTo File(mockMarathonFileDir, "./marathon").canonicalFile
        configuration.analyticsConfiguration shouldBeEqualTo AnalyticsConfiguration.DisabledAnalytics
        configuration.poolingStrategy shouldBeEqualTo PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration
        configuration.shardingStrategy shouldBeEqualTo ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration
        configuration.sortingStrategy shouldBeEqualTo SortingStrategyConfiguration.NoSortingStrategyConfiguration
        configuration.batchingStrategy shouldBeEqualTo BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration
        configuration.flakinessStrategy shouldBeEqualTo FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration
        configuration.retryStrategy shouldBeEqualTo RetryStrategyConfiguration.NoRetryStrategyConfiguration

        configuration.filteringConfiguration.allowlist.shouldBeEmpty()
        configuration.filteringConfiguration.blocklist.shouldBeEmpty()

        configuration.testClassRegexes.map { it.toString() } shouldContainSame listOf("^((?!Abstract).)*Test[s]*$")

        configuration.includeSerialRegexes shouldBeEqualTo emptyList()
        configuration.excludeSerialRegexes shouldBeEqualTo emptyList()
        configuration.ignoreFailures shouldBeEqualTo false
        configuration.isCodeCoverageEnabled shouldBeEqualTo false
        configuration.testBatchTimeoutMillis shouldBeEqualTo 1800_000
        configuration.testOutputTimeoutMillis shouldBeEqualTo 300_000
        configuration.debug shouldBeEqualTo true
        configuration.screenRecordingPolicy shouldBeEqualTo ScreenRecordingPolicy.ON_FAILURE
        configuration.vendorConfiguration shouldBeEqualTo VendorConfiguration.AndroidConfiguration(
            VendorConfiguration.AndroidConfiguration.VendorType.ADAM,
            File("/local/android"),
            File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
            File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
            null,
            null,
            null,
            false,
            mapOf(),
            false,
            false,
            30_000,
            "",
            SerialStrategy.AUTOMATIC
        )
    }



    @Test
    fun `on configuration without an explicit xctestrun path should throw an exception`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_5.yaml").file)
        val create = { parser.parse(file) }

        create shouldThrow ConfigurationException::class
    }

    @Test
    fun `on configuration without androidSdk value should use value provided by environment`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_6.yaml").file)
        val environmentReader = mockEnvironmentReader("/android/home")
        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
        mapper.registerModule(
            SerializeModule(
                mockInstantTimeProvider,
                mockMarathonFileDir,
            )
        )
            .registerModule(KotlinModule.Builder()
                                .withReflectionCacheSize(512)
                                .configure(KotlinFeature.NullToEmptyCollection, false)
                                .configure(KotlinFeature.NullToEmptyMap, false)
                                .configure(KotlinFeature.NullIsSameAsDefault, false)
                                .configure(KotlinFeature.SingletonSupport, true)
                                .configure(KotlinFeature.StrictNullChecks, false)
                                .build())
            .registerModule(JavaTimeModule())
        parser = ConfigurationFactory(marathonfileDir = mockMarathonFileDir, mapper = mapper, environmentReader = environmentReader)
        val configuration = parser.parse(file)

        configuration.vendorConfiguration shouldBeEqualTo VendorConfiguration.AndroidConfiguration(
            VendorConfiguration.AndroidConfiguration.VendorType.ADAM,
            environmentReader.read().androidSdk!!,
            File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
            File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
            null,
            null,
            null,
            false,
            mapOf(),
            false,
            false,
            30_000,
            "",
            SerialStrategy.HOSTNAME
        )
    }

    @Test
    fun `on configuration without androidSdk value should throw an exception when ANDROID_HOME is not set`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_7.yaml").file)
        parser =
            ConfigurationFactory(marathonfileDir = mockMarathonFileDir, mapper = mapper, environmentReader = mockEnvironmentReader(null))
        assertThrows<ConfigurationException> {
            parser.parse(file)
        }
    }

    @Test
    fun `on configuration with allowlist but no blocklist should initialize an empty blocklist`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_8.yaml").file)
        val configuration = parser.parse(file)

        configuration.filteringConfiguration.allowlist shouldBeEqualTo listOf(
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*".toRegex())
        )

        configuration.filteringConfiguration.blocklist shouldBe emptyList()
    }

    @Test
    fun `on configuration with blocklist but no allowlist should initialize an empty allowlist`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_9.yaml").file)
        val configuration = parser.parse(file)

        configuration.filteringConfiguration.allowlist shouldBe emptyList()

        configuration.filteringConfiguration.blocklist shouldBeEqualTo listOf(
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*".toRegex())
        )
    }

    @Test
    fun `on configuration time limits specified as Duration should be used as relative values`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_10.yaml").file)
        val configuration = parser.parse(file)

        configuration.sortingStrategy shouldBeInstanceOf SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration::class
        val sortingStrategy = configuration.sortingStrategy as SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration
        sortingStrategy.timeLimit shouldBeEqualTo referenceInstant.minus(Duration.ofHours(1))

        configuration.flakinessStrategy shouldBeInstanceOf FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration::class
        val flakinessStrategy =
            configuration.flakinessStrategy as FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration
        flakinessStrategy.timeLimit shouldBeEqualTo referenceInstant.minus(Duration.ofDays(30))
    }

    @Test
    fun `on configuration with timeout configuration in Android`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_11.yaml").file)
        val configuration = parser.parse(file)

        val timeoutConfiguration = (configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration).timeoutConfiguration
        timeoutConfiguration `should be equal to` TimeoutConfiguration(
            shell = Duration.ofSeconds(30),
            listFiles = Duration.ofMinutes(1),
            pushFile = Duration.ofHours(1),
            pullFile = Duration.ofDays(1),
            uninstall = Duration.ofSeconds(1),
            install = Duration.parse("P1DT12H30M5S"),
            screenrecorder = Duration.ofHours(1),
            screencapturer = Duration.ofSeconds(1),
            socketIdleTimeout = Duration.ofSeconds(45)
        )
    }

    @Test
    fun `on configuration with multi module testing in Android`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/sample_12.yaml").file)
        val configuration = parser.parse(file)

        val outputs = (configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration).outputs

        outputs shouldBeEqualTo listOf(
            AndroidTestBundleConfiguration(
                application = File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/debug/first-app-debug.apk"),
                testApplication = File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/first-app-debug-androidTest.apk"),
                extraApplications = null,
                splitApks = listOf(
                    File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/first-app-split-debug.apk")
                )
            ),
            AndroidTestBundleConfiguration(
                application = File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/debug/second-app-debug.apk"),
                testApplication = File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/second-app-debug-androidTest.apk"),
                extraApplications = null,
                splitApks = listOf(
                    File(mockMarathonFileDir, "kotlin-buildscript/build/outputs/apk/androidTest/debug/second-app-split-debug.apk")
                )
            )
        )
    }
}
