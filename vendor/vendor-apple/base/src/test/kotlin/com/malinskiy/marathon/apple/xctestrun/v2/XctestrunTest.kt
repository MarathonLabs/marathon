package com.malinskiy.marathon.apple.xctestrun.v2

import com.malinskiy.marathon.apple.plist.PropertyList
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File

class XctestrunTest {
    @Test
    fun testRead() {
        val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/man_v2.xctestrun").file)
        val actual: Xctestrun = PropertyList.from(file)

        val testTarget = TestTarget.withArtifactReinstall(
            name = "MyAppTests",
            testBundlePath = "__TESTHOST__/PlugIns/MyAppTests.xctest",
            testHostPath = "__TESTROOT__/Debug-iphonesimulator/MyApp.app",
            testingEnvironmentVariables = mapOf(
                "DYLD_FRAMEWORK_PATH" to "__TESTROOT__/Debug-iphonesimulator",
                "DYLD_LIBRARY_PATH" to "__TESTROOT__/Debug-iphonesimulator"
            ),
            uiTargetAppPath = "__TESTROOT__/Debug-iphonesimulator/MyApp.app",
            dependentProductPaths = arrayOf(
                "__TESTROOT__/Debug-iphonesimulator/MyApp.app",
                "__TESTROOT__/Debug-iphonesimulator/MyApp.app/PlugIns/MyAppTests.xctest"
            ),
            productModuleName = "MyAppTests",
            systemAttachmentLifetime = "deleteOnSuccess",
            userAttachmentLifetime = "deleteOnSuccess"
        )
        val testConfiguration = TestConfiguration(
            name = "Configuration 1",
            testTargets = arrayOf(
                testTarget
            )
        )
        val expected = Xctestrun(
            metadata = Metadata(formatVersion = 2),
            testPlan = TestPlan(
                name = "MyTestPlan",
                isDefault = true
            ),
            testConfigurations = arrayOf(
                testConfiguration
            ),
            codeCoverageBuildableInfos = arrayOf(
                CodeCoverageBuildableInfo(
                    architectures = arrayOf("x86_64"),
                    name = "MyApp.app",
                    buildableIdentifier = "1234567890ABCDEF:primary",
                    includeInReport = true,
                    isStatic = false,
                    productPaths = arrayOf("__TESTROOT__/Debug-iphonesimulator/MyApp.app/MyApp"),
                    sourceFiles = arrayOf("ViewController.swift", "AppDelegate.swift"),
                    sourceFilesCommonPathPrefix = "/Users/johnappleseed/MyApp/MyApp/",
                    toolchains = arrayOf("com.apple.dt.toolchain.XcodeDefault"),
                )
            )
        )

        actual shouldBeEqualTo expected
    }

    @Test
    fun testWrite() {
        val testTarget = TestTarget.withArtifactReinstall(
            name = "MyAppTests",
            testBundlePath = "__TESTHOST__/PlugIns/MyAppTests.xctest",
            testHostPath = "__TESTROOT__/Debug-iphonesimulator/MyApp.app",
            testingEnvironmentVariables = mapOf(
                "DYLD_FRAMEWORK_PATH" to "__TESTROOT__/Debug-iphonesimulator",
                "DYLD_LIBRARY_PATH" to "__TESTROOT__/Debug-iphonesimulator"
            ),
            uiTargetAppPath = "__TESTROOT__/Debug-iphonesimulator/MyApp.app",
            dependentProductPaths = arrayOf(
                "__TESTROOT__/Debug-iphonesimulator/MyApp.app",
                "__TESTROOT__/Debug-iphonesimulator/MyApp.app/PlugIns/MyAppTests.xctest"
            ),
            productModuleName = "MyAppTests",
            systemAttachmentLifetime = "deleteOnSuccess",
            userAttachmentLifetime = "deleteOnSuccess"
        )
        val testConfiguration = TestConfiguration(
            name = "Configuration 1",
            testTargets = arrayOf(
                testTarget
            )
        )
        val xctestrun = Xctestrun(
            metadata = Metadata(formatVersion = 2),
            testPlan = TestPlan(
                name = "MyTestPlan",
                isDefault = true
            ),
            testConfigurations = arrayOf(
                testConfiguration
            ),
            codeCoverageBuildableInfos = arrayOf(
                CodeCoverageBuildableInfo(
                    architectures = arrayOf("x86_64"),
                    name = "MyApp.app",
                    buildableIdentifier = "1234567890ABCDEF:primary",
                    includeInReport = true,
                    isStatic = false,
                    productPaths = arrayOf("__TESTROOT__/Debug-iphonesimulator/MyApp.app/MyApp"),
                    sourceFiles = arrayOf("ViewController.swift", "AppDelegate.swift"),
                    sourceFilesCommonPathPrefix = "/Users/johnappleseed/MyApp/MyApp/",
                    toolchains = arrayOf("com.apple.dt.toolchain.XcodeDefault"),
                )
            )
        )

        val outputStream = ByteArrayOutputStream()
        xctestrun.saveAsXML(outputStream)

        val expected = File(javaClass.classLoader.getResource("fixtures/xctestrun/expect_2.xctestrun").file).readText().trim()
        outputStream.toString() shouldBeEqualTo expected
    }
}
