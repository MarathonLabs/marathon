package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

class IOSTestParserSpek : Spek({
    describe("iOS test parser") {
        val parser = IOSTestParser()

        on("project sources") {
            val sourceRoot = File(javaClass.classLoader.getResource("src/sample-xcworkspace/sample-appUITests").file)
            val xctestrunPath = File(javaClass.classLoader.getResource("src/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
            val iosConfiguration = IOSConfiguration(xctestrunPath)
            val configuration = Configuration("name", File(""), File(""), File(""), sourceRoot, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, iosConfiguration)

            it("should return accurate list of tests") {
                val extractedTests = parser.extract(configuration)

                extractedTests shouldEqual listOf(
                        Test("sample_appUITests", "StoryboardTests", "testButton", emptyList()),
                        Test("sample_appUITests", "StoryboardTests", "testLabel", emptyList()),
                        Test("sample_appUITests", "MoreTests", "testPresentModal", emptyList())
                )
            }
        }
    }
})