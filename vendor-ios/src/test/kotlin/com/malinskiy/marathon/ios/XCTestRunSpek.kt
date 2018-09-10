package com.malinskiy.marathon.ios

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

class XCTestRunSpek : Spek({
    describe("XCTestRun") {
        on("parsing") {
            val file = File(javaClass.classLoader.getResource("src/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
            val xctestrun = XCTestRun(file)

            it("should load properties") {
                xctestrun.moduleName shouldEqual "sample_appUITests"
                xctestrun.isUITestBundle shouldEqual true
            }

            it("should accurately determine skipped tests") {
                val test1 = Test("sample_appUITests", "SkippedSuite", "anyTest", listOf())
                val test2 = Test("sample_appUITests", "StoryboardTests", "testDisabledButton", listOf())
                val test3 = Test("sample_appUITests", "StoryboardTests", "testLabel", listOf())

                xctestrun.isSkipped(test1) shouldEqual true
                xctestrun.isSkipped(test2) shouldEqual true
                xctestrun.isSkipped(test3) shouldEqual false
            }
        }
    }
})
