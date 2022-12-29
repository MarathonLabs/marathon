package com.malinskiy.marathon.ios.xctestrun.v1

import com.malinskiy.marathon.ios.plist.PropertyList
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainSame
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File

class XctestrunTest {
    @Test
    fun testRead() {
        val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
        val xctestrun: Xctestrun = PropertyList.from(file)
        xctestrun.targets.keys shouldContainSame setOf("sample-appUITests")
        val target = xctestrun.targets.values.first()
        target.isUITestBundle shouldBeEqualTo true
        target.systemAttachmentLifetime shouldBeEqualTo "deleteOnSuccess"
        target.testBundlePath shouldBeEqualTo "__TESTHOST__/PlugIns/sample-appUITests.xctest"
        target.testHostPath shouldBeEqualTo "__TESTROOT__/Debug-iphonesimulator/sample-appUITests-Runner.app"
        target.uiTargetAppPath shouldBeEqualTo "__TESTROOT__/Debug-iphonesimulator/sample-app.app"
        target.userAttachmentLifetime shouldBeEqualTo "deleteOnSuccess"
        target.dependentProductPaths shouldContainSame setOf(
            "__TESTROOT__/Debug-iphonesimulator/sample-appUITests-Runner.app/PlugIns/sample-appUITests.xctest",
            "__TESTROOT__/Debug-iphonesimulator/sample-app.app"
        )

        target.testingEnvironmentVariables shouldContainSame mapOf(
            "DYLD_FRAMEWORK_PATH" to "__TESTROOT__/Debug-iphonesimulator:__PLATFORMS__/iPhoneSimulator.platform/Developer/Library/Frameworks:/Library/Developer/CoreSimulator/Profiles/Runtimes/iOS 11.1.simruntime/Contents/Resources/RuntimeRoot/Developer/Library/PrivateFrameworks",
            "DYLD_INSERT_LIBRARIES" to "/Library/Developer/CoreSimulator/Profiles/Runtimes/iOS 11.1.simruntime/Contents/Resources/RuntimeRoot/usr/lib/libMainThreadChecker.dylib",
            "DYLD_LIBRARY_PATH" to "__TESTROOT__/Debug-iphonesimulator:__PLATFORMS__/iPhoneSimulator.platform/Developer/Library/Frameworks:/Library/Developer/CoreSimulator/Profiles/Runtimes/iOS 11.1.simruntime/Contents/Resources/RuntimeRoot/Developer/Library/PrivateFrameworks",
            "MTC_CRASH_ON_REPORT" to "1",
            "XCODE_DBG_XPC_EXCLUSIONS" to "com.apple.dt.xctestSymbolicator",
        )
        target.productModuleName shouldBeEqualTo "sample_appUITests"
        target.environmentVariables shouldContainSame mapOf(
            "DYLD_FRAMEWORK_PATH" to "/Users/pkunzip/Development/marathon/vendor-ios/src/test/resources/src/sample-xcworkspace/a/Build/Products/Debug-iphonesimulator",
            "DYLD_LIBRARY_PATH" to "/Users/pkunzip/Development/marathon/vendor-ios/src/test/resources/src/sample-xcworkspace/a/Build/Products/Debug-iphonesimulator",
            "OS_ACTIVITY_DT_MODE" to "YES",
            "__XCODE_BUILT_PRODUCTS_DIR_PATHS" to "/Users/pkunzip/Development/marathon/vendor-ios/src/test/resources/src/sample-xcworkspace/a/Build/Products/Debug-iphonesimulator",
            "__XPC_DYLD_FRAMEWORK_PATH" to "/Users/pkunzip/Development/marathon/vendor-ios/src/test/resources/src/sample-xcworkspace/a/Build/Products/Debug-iphonesimulator",
            "__XPC_DYLD_LIBRARY_PATH" to "/Users/pkunzip/Development/marathon/vendor-ios/src/test/resources/src/sample-xcworkspace/a/Build/Products/Debug-iphonesimulator",
        )
    }

    @Test
    fun testWrite() {
        val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
        val xctestrun: Xctestrun = PropertyList.from(file)
        val target = xctestrun.targets.values.first()
        target.testingEnvironmentVariables = mapOf(
            "X" to "A",
            "Y" to "B",
        )
        target.dependentProductPaths = arrayOf(
            "pathA", "pathB"
        )
        val outputStream = ByteArrayOutputStream()
        xctestrun.saveAsXML(outputStream)

        val expected = File(javaClass.classLoader.getResource("fixtures/xctestrun/expect_1.xctestrun").file).readText().trim()
        outputStream.toString() shouldBeEqualTo expected
    }
}
