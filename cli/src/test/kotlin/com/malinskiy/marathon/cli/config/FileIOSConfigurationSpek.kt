package com.malinskiy.marathon.cli.config


import com.malinskiy.marathon.cli.args.FileIOSConfiguration
import com.malinskiy.marathon.cli.args.FileListProvider
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import java.io.File

object FileIOSConfigurationSpek : Spek({
    describe("Building an iOS file configuration") {

        val mockXctestrunFile = File("Build/uitesting.xctestrun")
        var mockDerivedDataFiles =  { emptySequence<File>() }
        val mockFileListProvider = object : FileListProvider {
            override fun fileList(root: File): Sequence<File> = mockDerivedDataFiles()
        }

        context("when xctestrun is not specified") {
            val fileIOSConfiguration = FileIOSConfiguration(
                    File("a"),
                    null,
                    "user",
                    File("key"),
                    File("sourceRoot"),
                    mockFileListProvider)

            it("should search for such file under derived data folder") {
                mockDerivedDataFiles = { sequenceOf(File("."), mockXctestrunFile, File("runner.app")) }

                val iosConfiguration = fileIOSConfiguration.toIOSConfiguration(null, null)

                iosConfiguration.xctestrunPath shouldEqual mockXctestrunFile
            }

            it("should throw an exception if such file is not available") {
                mockDerivedDataFiles =  { sequenceOf(File("."), File("runner.app")) }

                val thrower = { val iosConfiguration = fileIOSConfiguration.toIOSConfiguration(null, null) }

                thrower shouldThrow ConfigurationException::class
            }
        }
    }
})