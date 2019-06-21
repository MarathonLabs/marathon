package com.malinskiy.marathon.ios.xctestrun

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldHaveKey
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldNotHaveKey
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import java.io.ByteArrayInputStream
import java.io.File

object XctestrunSpek : Spek({
    val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator12.1-x86_64-multitarget.xctestrun").file)

    describe("Xctestrun") {
        given("A parsed instance") {
            val xctestrun by memoized { Xctestrun(file) }

            it("should contain accurate values") {
                xctestrun.targetNames shouldContainSame listOf("sample-appUITests", "another-targetUITests")
                xctestrun.isUITestBundle("sample-appUITests") shouldEqual true
                xctestrun.isUITestBundle("another-targetUITests") shouldEqual true
                xctestrun.productModuleName("sample-appUITests") shouldEqual "sample_appUITests"
                xctestrun.productModuleName("another-targetUITests") shouldEqual "another_targetUITests"
            }

            it("should accurately determine skipped tests") {
                val test1 = Test("sample-appUITests", "SkippedSuite", "anyTest", listOf())
                val test2 = Test("sample-appUITests", "StoryboardTests", "testDisabledButton", listOf())
                val test3 = Test("sample-appUITests", "StoryboardTests", "testLabel", listOf())
                val test4 = Test("another-targetUITests", "Patience", "testLabel", listOf())

                xctestrun.isSkipped(test1) shouldEqual true
                xctestrun.isSkipped(test2) shouldEqual true
                xctestrun.isSkipped(test3) shouldEqual false
                xctestrun.isSkipped(test4) shouldEqual false
            }
        }
        given("A valid instance") {
            val xctestrun by memoized { Xctestrun(file) }

            it("should verify equality ignoring key order") {
                val reorderedFile = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator12.1-x86_64-reordered.xctestrun").file)
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

            it("should be able to modify target environment variables") {
                xctestrun.environment("sample-appUITests", "SPEK_DEBUG", "YES")

                val appVariables = xctestrun.environmentVariables("sample-appUITests")
                appVariables shouldNotBe null
                appVariables!! shouldHaveKey "SPEK_DEBUG"
                appVariables["SPEK_DEBUG"] shouldEqual "YES"

                val anotherTargetVariables = xctestrun.environmentVariables("another-targetUITests")
                anotherTargetVariables shouldNotBe null
                anotherTargetVariables!! shouldNotHaveKey "SPEK_DEBUG"
            }

            it("should be able to modify testing environment variables") {
                xctestrun.testingEnvironment("sample-appUITests", "SPEK_DEBUG", "YES")

                val appVariables = xctestrun.testingEnvironmentVariables("sample-appUITests")
                appVariables shouldNotBe null
                appVariables!! shouldHaveKey "SPEK_DEBUG"
                appVariables["SPEK_DEBUG"] shouldEqual "YES"

                val anotherTargetVariables = xctestrun.testingEnvironmentVariables("another-targetUITests")
                anotherTargetVariables shouldNotBe null
                anotherTargetVariables!! shouldNotHaveKey "SPEK_DEBUG"
            }

            it("should not update clone when source changes") {
                val clone = xctestrun.clone()

                xctestrun.environment("sample-appUITests", "SPEK_DEBUG", "YES")
                xctestrun.testingEnvironment("another-targetUITests", "SPEK_DEBUG", "YES")

                clone.environmentVariables("sample-appUITests")!! shouldNotHaveKey "SPEK_DEBUG"
                clone.testingEnvironmentVariables("another-targetUITests")!! shouldNotHaveKey "SPEK_DEBUG"
            }
        }
        given("A valid file with metadata key") {
            val updatedFile = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator12.1-x86_64.xctestrun").file)
            val xctestrun by memoized { Xctestrun(updatedFile) }

            it("should accurately determine the testable target key") {
                xctestrun.targetNames shouldContainSame listOf("sample-appUITests")
            }
        }
    }
})
