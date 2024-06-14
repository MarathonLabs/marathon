package com.malinskiy.marathon.config.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.malinskiy.marathon.config.serialization.time.InstantTimeProvider
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.AppleTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.apple.ios.LifecycleConfiguration
import com.malinskiy.marathon.config.vendor.apple.RsyncConfiguration
import com.malinskiy.marathon.config.vendor.apple.SshAuthentication
import com.malinskiy.marathon.config.vendor.apple.SshConfiguration
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration
import java.time.Instant
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.config.serialization.yaml.SerializeModule
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.config.vendor.apple.TestType
import com.malinskiy.marathon.config.vendor.apple.ios.PullingPolicy
import com.malinskiy.marathon.config.vendor.apple.ios.XcresultConfiguration
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldHaveSize

class IosConfigurationFactoryTest {
    private val mockMarathonFileDir = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios").file)
    private val referenceInstant: Instant = Instant.ofEpochSecond(1000000)
    private val mockInstantTimeProvider = object : InstantTimeProvider {
        override fun referenceTime(): Instant = referenceInstant
    }
    private val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)).apply {
        registerModule(
            SerializeModule(
                mockInstantTimeProvider,
                mockMarathonFileDir,
            )
        )
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, true)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        registerModule(JavaTimeModule())
    }
    private val parser: ConfigurationFactory = ConfigurationFactory(
        marathonfileDir = mockMarathonFileDir,
        mapper = mapper,
    )

    @Test
    fun `on config with ios vendor configuration should initialize a specific vendor configuration`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios/sample_2.yaml").file)
        val configuration = parser.parse(file)

        configuration.vendorConfiguration shouldBeEqualTo VendorConfiguration.IOSConfiguration(
            bundle = AppleTestBundleConfiguration(
                derivedDataDir = file.parentFile.resolve("derivedDataDir").canonicalFile,
            ),
            ssh = SshConfiguration(
                authentication = SshAuthentication.PublicKeyAuthentication(
                    username = "testuser",
                    key = File("/home/testuser/.ssh/id_rsa").canonicalFile
                ),
                knownHostsPath = file.parentFile.resolve("known_hosts").canonicalFile,
                keepAliveInterval = Duration.ofSeconds(300L),
                debug = true,
            ),
            lifecycleConfiguration = LifecycleConfiguration(
                onPrepare = emptySet()
            ),
            rsync = RsyncConfiguration(
                remotePath = "/usr/local/bin/rsync",
            ),
            xcresult = XcresultConfiguration(
                pullingPolicy = PullingPolicy.ON_FAILURE,
                remoteClean = false
            ),
            hideRunnerOutput = true,
            compactOutput = true,
            devicesFile = file.parentFile.resolve("Testdevices").canonicalFile,
        )
    }

    @Test
    fun `on configuration without an explicit remote rsync path should initialize a default one`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios/sample_3.yaml").file)
        val configuration = parser.parse(file)

        val iosConfiguration = configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration
        iosConfiguration.rsync.remotePath shouldBeEqualTo "/usr/bin/rsync"
        iosConfiguration.bundle?.testType shouldBeEqualTo TestType.XCUITEST
    }

    @Test
    fun `on configuration with xctest type specified`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios/sample_3.yaml").file)
        val configuration = parser.parse(file)

        val iosConfiguration = configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration
        iosConfiguration.bundle?.testType shouldBeEqualTo TestType.XCUITEST
    }

    @Test
    fun `on configuration with import media files`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios/sample_4.yaml").file)
        val configuration = parser.parse(file)

        val iosConfiguration = configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration
        iosConfiguration.mediaFiles?.shouldHaveSize(2)
        iosConfiguration.mediaFiles?.shouldContain(file.parentFile.resolve("media/empty.jpg").canonicalFile)
        iosConfiguration.mediaFiles?.shouldContain(file.parentFile.resolve("media/empty.mp4").canonicalFile)
    }

    @Test
    fun `on configuration with timeout configuration in iOS`() {
        val file = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios/sample_5.yaml").file)
        val configuration = parser.parse(file)

        val timeoutConfiguration = (configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration).timeoutConfiguration
        timeoutConfiguration `should be equal to` TimeoutConfiguration(
            shell = Duration.ofSeconds(30),
            shellIdle = Duration.ofSeconds(31),
            reachability = Duration.ofMinutes(1),
            screenshot = Duration.ofMinutes(2),
            video = Duration.ofHours(1),
            erase = Duration.ofHours(2),
            shutdown = Duration.ofDays(1),
            delete = Duration.ofSeconds(3),
            create = Duration.parse("P1DT12H30M5S"),
            boot = Duration.ofSeconds(80),
            install = Duration.ofHours(3),
            uninstall = Duration.ofSeconds(62),
            importMedia = Duration.ofSeconds(6),
            testDestination = Duration.ofSeconds(34),
        )
    }
}
