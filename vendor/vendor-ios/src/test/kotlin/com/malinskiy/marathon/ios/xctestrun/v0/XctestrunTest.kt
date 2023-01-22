package com.malinskiy.marathon.ios.xctestrun.v0

import com.malinskiy.marathon.ios.plist.PropertyList
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File

class XctestrunTest {
    @Test
    fun testRead() {
        val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/man_v0.xctestrun").file)
        val actual: Xctestrun = PropertyList.from(file)

        val target = TestTarget(
            testBundlePath = "__TESTHOST__/PlugIns/Tests.xctest",
            testHostPath = "__TESTROOT__/App-Runner.app",
            testingEnvironmentVariables = mapOf(
                "DYLD_FRAMEWORK_PATH" to "__TESTROOT__:__PLATFORMS__/iPhoneOS.platform/Developer/Library/Frameworks",
                "DYLD_LIBRARY_PATH" to "__TESTROOT__:__PLATFORMS__/iPhoneOS.platform/Developer/Library/Frameworks",
                "XCODE_DBG_XPC_EXCLUSIONS" to "com.apple.dt.xctestSymbolicator",
            )
        )
        target.uiTargetAppPath = "__TESTROOT__/Target.app"
        val expected = Xctestrun("TestTargetName", target)
        
        actual shouldBeEqualTo expected
    }

    @Test
    fun testWrite() {
        val xctestrun = TestTarget(
            testBundlePath = "__TESTHOST__/PlugIns/Tests.xctest",
            testHostPath = "__TESTROOT__/App-Runner.app",
            testingEnvironmentVariables = mapOf(
                "DYLD_FRAMEWORK_PATH" to "__TESTROOT__:__PLATFORMS__/iPhoneOS.platform/Developer/Library/Frameworks",
                "DYLD_LIBRARY_PATH" to "__TESTROOT__:__PLATFORMS__/iPhoneOS.platform/Developer/Library/Frameworks",
                "XCODE_DBG_XPC_EXCLUSIONS" to "com.apple.dt.xctestSymbolicator",
            )
        )
        xctestrun.uiTargetAppPath = "__TESTROOT__/Target.app"
        
        val outputStream = ByteArrayOutputStream()
        xctestrun.saveAsXML(outputStream)

        val expected = File(javaClass.classLoader.getResource("fixtures/xctestrun/expect_0.xctestrun").file).readText().trim()
        outputStream.toString() shouldBeEqualTo expected
    }
}
