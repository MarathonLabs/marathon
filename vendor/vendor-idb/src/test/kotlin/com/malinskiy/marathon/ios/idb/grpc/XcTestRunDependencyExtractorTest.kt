package com.malinskiy.marathon.ios.idb.grpc

import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test
import java.io.File


class XcTestRunDependencyExtractorTest{
    @Test
    fun testExtractDependencies() {
        val input = File("src/test/resources/EEE7D1D8-E6D6-4D0A-9531-F955F4EA3523.xctestrun")
        val results = XcTestRunParser().extractArtifacts(input)
        results.size shouldEqualTo 2
        results shouldContainAll listOf(
            "src/test/resources/Debug-iphonesimulator/sample-app.app",
            "src/test/resources/Debug-iphonesimulator/sample-appUITests-Runner.app/PlugIns/sample-appUITests.xctest"
        )
    }
}
