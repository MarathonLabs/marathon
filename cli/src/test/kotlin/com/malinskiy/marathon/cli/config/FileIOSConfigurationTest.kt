package com.malinskiy.marathon.cli.config


import com.malinskiy.marathon.cli.args.FileIOSConfiguration
import com.malinskiy.marathon.cli.args.FileListProvider
import com.malinskiy.marathon.exceptions.ConfigurationException
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File

class FileIOSConfigurationTest {
    val mockMarathonFileDir = File("")
    val mockXctestrunFile = File("Build/uitesting.xctestrun")
    var mockDerivedDataFiles = { emptySequence<File>() }
    val mockFileListProvider = object : FileListProvider {
        override fun fileList(root: File): Iterable<File> = mockDerivedDataFiles().asIterable()
    }
    val fileIOSConfiguration = FileIOSConfiguration(
        derivedDataDir = File("a"),
        xctestrunPath = null,
        remoteUsername = "user",
        remotePrivateKey = File("key"),
        knownHostsPath = null,
        sourceRoot = File("sourceRoot"),
        fileListProvider = mockFileListProvider,
        debugSsh = null,
        alwaysEraseSimulators = true,
        hideRunnerOutput = null,
        devices = null
    )

    @Test
    fun `when xctestrun is not specified should search for such file under derived data folder`() {
        mockDerivedDataFiles =
            { sequenceOf(File("."), mockXctestrunFile, File("runner.app")) }

        val iosConfiguration =
            fileIOSConfiguration.toIOSConfiguration(mockMarathonFileDir, null)

        iosConfiguration.xctestrunPath shouldEqual mockXctestrunFile
    }

    @Test
    fun `when xctestrun is not specified should throw an exception if such file is not available`() {
        mockDerivedDataFiles = { sequenceOf(File("."), File("runner.app")) }

        val thrower = {
            val iosConfiguration =
                fileIOSConfiguration.toIOSConfiguration(mockMarathonFileDir, null)
        }

        thrower shouldThrow ConfigurationException::class
    }
}
