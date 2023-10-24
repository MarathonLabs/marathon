package com.malinskiy.marathon.config.serialization

import com.malinskiy.marathon.config.LogicalConfigurationValidator
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Path

class ConfigurationSerializationTest {

    val validator = LogicalConfigurationValidator()
    
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
            //should fail due to no xctestrun file
            //"sample_5.yaml"
            //"ios/sample_1.yaml",
            "ios/sample_1.yaml",
            "ios/sample_2.yaml",
            "ios/sample_3.yaml",
        )
    )
    @ParameterizedTest
    fun testSimple(path: String, @TempDir tempDir: Path) {
        val file = File(ConfigurationSerializationTest::class.java.getResource("/fixture/config/${path}").file)

        val configurationFactory = ConfigurationFactory(file.parentFile)
        val parsedConfiguration = configurationFactory.parse(file)

        val serializedYaml = configurationFactory.serialize(parsedConfiguration)
        val tempFile = File(tempDir.toFile(), "temp")
        tempFile.writeText(serializedYaml)

        val recreatedConfiguration = configurationFactory.parse(tempFile)

        parsedConfiguration shouldBeEqualTo recreatedConfiguration
    }
}
