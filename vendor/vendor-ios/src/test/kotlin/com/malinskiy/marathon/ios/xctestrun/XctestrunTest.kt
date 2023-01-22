//package com.malinskiy.marathon.ios.xctestrun
//
//import com.malinskiy.marathon.ios.xctestrun.legacy.Xctestrun
//import org.amshove.kluent.shouldBeEqualTo
//import org.amshove.kluent.shouldHaveKey
//import org.amshove.kluent.shouldNotHaveKey
//import org.junit.jupiter.api.Test
//import java.io.ByteArrayInputStream
//import java.io.File
//import com.malinskiy.marathon.test.Test as MarathonTest
//
//class XctestrunTest {
//    private val file = File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
//    private val xctestrun = Xctestrun(file)
//
//    @Test
//    fun `parsing should return correct property values`() {
//        xctestrun.targetName shouldBeEqualTo "sample-appUITests"
//        xctestrun.isUITestBundle shouldBeEqualTo true
//    }
//
//    @Test
//    fun `parsing should accurately determine skipped tests`() {
//        val test1 = MarathonTest("sample-appUITests", "SkippedSuite", "anyTest", listOf())
//        val test2 = MarathonTest("sample-appUITests", "StoryboardTests", "testDisabledButton", listOf())
//        val test3 = MarathonTest("sample-appUITests", "StoryboardTests", "testLabel", listOf())
//
//        xctestrun.isSkipped(test1) shouldBeEqualTo true
//        xctestrun.isSkipped(test2) shouldBeEqualTo true
//        xctestrun.isSkipped(test3) shouldBeEqualTo false
//    }
//
//    @Test
//    fun `a valid instance should verify equality ignoring key order`() {
//        val reorderedFile =
//            File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator11.2-x86_64-reordered.xctestrun").file)
//        val reordered = Xctestrun(reorderedFile)
//
//        reordered shouldBeEqualTo xctestrun
//    }
//
//    @Test
//    fun `a valid instance should accurately serialize and deserialize`() {
//        val other = Xctestrun(
//            ByteArrayInputStream(
//                xctestrun.toXMLString().toByteArray()
//            )
//        )
//
//        other shouldBeEqualTo xctestrun
//    }
//
//    @Test
//    fun `a valid instance should be equal to its clone`() {
//        val clone = xctestrun.clone()
//
//        clone shouldBeEqualTo xctestrun
//    }
//
//    @Test
//    fun `a valid instance should be able to modify environment variables`() {
//        xctestrun.environment("SPEK_DEBUG", "YES")
//
//        xctestrun.environmentVariables shouldHaveKey "SPEK_DEBUG"
//        xctestrun.environmentVariables["SPEK_DEBUG"] shouldBeEqualTo "YES"
//    }
//
//    @Test
//    fun `a valid instance should be able to modify testing environment variables`() {
//        xctestrun.testingEnvironment("SPEK_DEBUG", "YES")
//
//        xctestrun.testingEnvironmentVariables shouldHaveKey "SPEK_DEBUG"
//        xctestrun.testingEnvironmentVariables["SPEK_DEBUG"] shouldBeEqualTo "YES"
//    }
//
//    @Test
//    fun `a valid instance should not update clone when source changes`() {
//        val clone = xctestrun.clone()
//
//        xctestrun.environment("SPEK_DEBUG", "YES")
//        xctestrun.testingEnvironment("SPEK_DEBUG", "YES")
//
//        clone.environmentVariables shouldNotHaveKey "SPEK_DEBUG"
//        clone.testingEnvironmentVariables shouldNotHaveKey "SPEK_DEBUG"
//    }
//
//    @Test
//    fun `a valid file with metadata key should accurately determine the testable target key`() {
//        val updatedFile =
//            File(javaClass.classLoader.getResource("fixtures/xctestrun/UITesting_iphonesimulator12.1-x86_64.xctestrun").file)
//        val xctestrun = Xctestrun(updatedFile)
//        xctestrun.targetName shouldBeEqualTo "sample-appUITests"
//    }
//}
