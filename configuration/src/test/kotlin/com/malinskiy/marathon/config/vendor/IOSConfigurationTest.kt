package com.malinskiy.marathon.config.vendor


import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.serialization.ConfigurationFactoryTest
import com.malinskiy.marathon.config.serialization.yaml.FileListProvider
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class IOSConfigurationTest {
    private val mockMarathonFileDir = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios").file)
    val mockXctestFile = File(ConfigurationFactoryTest::class.java.getResource("/fixture/config/ios/derivedDataDir/ui.xctest").file)

    @Test
    fun `when applications are not specified should search for them under derived data folder`() {
        val marathonfile = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/sample_1.yaml").file)
        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
        )

        val configuration = configurationFactory.parse(marathonfile)
        (configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration).bundle?.xctest shouldBeEqualTo mockXctestFile
    }

    @Test
    fun `when xctest is not specified should throw an exception if such file is not available`() {
        val thrower = {
            val marathonfile = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/sample_1.yaml").file)
            val configurationFactory = ConfigurationFactory(
                marathonfileDir = createTempDirectory("xctest").toFile(),
            )

            val configuration = configurationFactory.parse(marathonfile)
            (configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration).bundle?.xctest shouldBeEqualTo mockXctestFile
        }

        thrower shouldThrow ConfigurationException::class
    }
}
