package com.malinskiy.marathon.config.vendor


import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.serialization.yaml.FileListProvider
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File

class IOSConfigurationTest {
    val mockMarathonFileDir = File("")
    val mockXctestrunFile = File("Build/uitesting.xctestrun")
    var mockDerivedDataFiles = { emptySequence<File>() }
    val mockFileListProvider = object : FileListProvider {
        override fun fileList(root: File): Iterable<File> = mockDerivedDataFiles().asIterable()
    }

    @Test
    fun `when xctestrun is not specified should search for such file under derived data folder`() {
        mockDerivedDataFiles =
            { sequenceOf(File("."), mockXctestrunFile, File("runner.app")) }

        val marathonfile = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/sample_1.yaml").file)
        val configurationFactory = ConfigurationFactory(
            marathonfileDir = mockMarathonFileDir,
            fileListProvider = mockFileListProvider,
        )

        val configuration = configurationFactory.parse(marathonfile)
        (configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration).bundle?.first()?.testApplication shouldBeEqualTo mockXctestrunFile
    }

    @Test
    fun `when xctestrun is not specified should throw an exception if such file is not available`() {
        mockDerivedDataFiles = { sequenceOf(File("."), File("runner.app")) }

        val thrower = {
            val marathonfile = File(IOSConfigurationTest::class.java.getResource("/fixture/config/ios/sample_1.yaml").file)
            val configurationFactory = ConfigurationFactory(
                marathonfileDir = mockMarathonFileDir,
                fileListProvider = mockFileListProvider,
            )

            val configuration = configurationFactory.parse(marathonfile)
            (configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration).bundle?.first()?.testApplication shouldBeEqualTo mockXctestrunFile
        }

        thrower shouldThrow ConfigurationException::class
    }
}
