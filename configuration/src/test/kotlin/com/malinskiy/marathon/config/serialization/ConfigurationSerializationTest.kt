package com.malinskiy.marathon.config.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.config.LogicalConfigurationValidator
import com.malinskiy.marathon.config.serialization.time.InstantTimeProvider
import com.malinskiy.marathon.config.serialization.time.InstantTimeProviderImpl
import com.malinskiy.marathon.config.serialization.yaml.SerializeModule
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Path
import java.time.Instant

class ConfigurationSerializationTest {

    val validator = LogicalConfigurationValidator()

    val referenceInstant: Instant = Instant.now()
    private val mockInstantTimeProvider = object : InstantTimeProvider {
        override fun referenceTime(): Instant = referenceInstant
    }

    lateinit var parser: ConfigurationFactory
    lateinit var mapper: ObjectMapper
    
    @ValueSource(
        strings = arrayOf(
            "sample_1.yaml",
            "sample_1_rp.yaml",
            "sample_2.yaml",
            "sample_6.yaml",
            "sample_7.yaml",
            "sample_8.yaml",
            "sample_9.yaml",
            "sample_10.yaml",
            "sample_11.yaml",
            "android/sample_1.yaml",
            "android/sample_2.yaml",
            "android/sample_3.yaml",
            "android/sample_4.yaml",
            "android/sample_5.yaml",
            "ios/sample_1.yaml",
            "ios/sample_2.yaml",
            "ios/sample_3.yaml",
        )
    )
    @ParameterizedTest
    fun testSimple(path: String, @TempDir tempDir: Path) {
        val file = File(ConfigurationSerializationTest::class.java.getResource("/fixture/config/${path}").file)

        val marathonfileDir = file.parentFile
        mapper = ObjectMapper(
            YAMLFactory()
                .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
        ).apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            registerModule(SerializeModule(mockInstantTimeProvider, marathonfileDir))
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
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
        parser = ConfigurationFactory(
            marathonfileDir = marathonfileDir,
            mapper = mapper,
        )

        val parsedConfiguration = parser.parse(file)

        val serializedYaml = parser.serialize(parsedConfiguration)
        val tempFile = File(tempDir.toFile(), "temp")
        tempFile.writeText(serializedYaml)

        val recreatedConfiguration = parser.parse(tempFile)

        parsedConfiguration shouldBeEqualTo recreatedConfiguration
    }
}
