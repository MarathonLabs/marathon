package com.malinskiy.marathon.ios.xctestrun

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldHaveKey
import org.amshove.kluent.shouldNotHaveKey
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.ByteArrayInputStream
import java.io.File

object XctestrunSpek : Spek({
    val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)

    describe("Xctestrun") {
        on("parsing") { 
            val xctestrun by memoized { Xctestrun(file) }

            it("should return correct property values") {
                xctestrun.targetName shouldEqual "sample-appUITests"
                xctestrun.isUITestBundle shouldEqual true
            }

            it("should accurately determine skipped tests") {
                val test1 = Test("sample-appUITests", "SkippedSuite", "anyTest", listOf())
                val test2 = Test("sample-appUITests", "StoryboardTests", "testDisabledButton", listOf())
                val test3 = Test("sample-appUITests", "StoryboardTests", "testLabel", listOf())

                xctestrun.isSkipped(test1) shouldEqual true
                xctestrun.isSkipped(test2) shouldEqual true
                xctestrun.isSkipped(test3) shouldEqual false
            }
        }
        given("A valid instance") {
            val xctestrun by memoized { Xctestrun(file) }

            it("should verify equality ignoring key order") {
                val reorderedFile = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator11.2-x86_64-reordered.xctestrun").file)
                val reordered = Xctestrun(reorderedFile)

                reordered shouldEqual xctestrun
            }

            it("should accurately serialize and deserialize") {
                val other = Xctestrun(
                        ByteArrayInputStream(
                                xctestrun.toXMLString().toByteArray()
                        )
                )

                other shouldEqual xctestrun
            }

            it("should be equal to its clone") {
                val clone = xctestrun.clone()

                clone shouldEqual xctestrun
            }

            it("should be able to modify environment variables") {
                xctestrun.environment("SPEK_DEBUG", "YES")

                xctestrun.environmentVariables shouldHaveKey "SPEK_DEBUG"
                xctestrun.environmentVariables["SPEK_DEBUG"] shouldEqual "YES"
            }

            it("should be able to modify testing environment variables") {
                xctestrun.testingEnvironment("SPEK_DEBUG", "YES")

                xctestrun.testingEnvironmentVariables shouldHaveKey "SPEK_DEBUG"
                xctestrun.testingEnvironmentVariables["SPEK_DEBUG"] shouldEqual "YES"
            }

            it("should not update clone when source changes") {
                val clone = xctestrun.clone()

                xctestrun.environment("SPEK_DEBUG", "YES")
                xctestrun.testingEnvironment("SPEK_DEBUG", "YES")

                clone.environmentVariables shouldNotHaveKey "SPEK_DEBUG"
                clone.testingEnvironmentVariables shouldNotHaveKey "SPEK_DEBUG"
            }
        }
    }
})
