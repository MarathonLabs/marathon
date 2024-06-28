package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class TestPackageFilterTest {
    private val simpleTest = stubTest("com.example")
    private val simpleBTest = stubTest("com.example2")
    private val complexTest = stubTest("com.example.subpackage")
    private val someClass = stubTest("com.sample")

    val tests = listOf(
        simpleTest,
        complexTest,
        someClass
    )

    @Test
    fun shouldFilterByRegex() {
        TestFilterConfiguration.TestPackageFilterConfiguration(regex = """com\.example.*""".toRegex()).toTestFilter()
            .filter(tests) shouldBeEqualTo listOf(simpleTest, complexTest)
    }

    @Test
    fun shouldFilterNotByRegex() {
        TestFilterConfiguration.TestPackageFilterConfiguration(regex = """com\.example.*""".toRegex()).toTestFilter()
            .filterNot(tests) shouldBeEqualTo listOf(someClass)
    }

    @Test
    fun shouldFilterByValues() {
        TestFilterConfiguration.TestPackageFilterConfiguration(values = listOf("com.example"), subpackages = false).toTestFilter().filter(tests) shouldBeEqualTo listOf(simpleTest)
    }

    @Test
    fun shouldFilterNotByValues() {
        TestFilterConfiguration.TestPackageFilterConfiguration(values = listOf("com.example"), subpackages = false).toTestFilter().filterNot(tests) shouldBeEqualTo listOf(complexTest, someClass)
    }

    @Test
    fun shouldFilterByValuesWithSubpackages() {
        TestFilterConfiguration.TestPackageFilterConfiguration(values = listOf("com.example"), subpackages = true).toTestFilter().filter(tests) shouldBeEqualTo listOf(simpleTest, complexTest)
    }

    @Test
    fun shouldFilterByValuesWithSubpackagesByPartialPackageName() {
        TestFilterConfiguration.TestPackageFilterConfiguration(values = listOf("com.example"), subpackages = true).toTestFilter().filter(listOf(simpleBTest)) shouldBeEqualTo emptyList()
    }

    @Test
    fun shouldFilterNotByValuesWithSubpackages() {
        TestFilterConfiguration.TestPackageFilterConfiguration(values = listOf("com.example"), subpackages = true).toTestFilter().filterNot(tests) shouldBeEqualTo listOf(someClass)
    }

    @Test
    fun disabled() {
        TestFilterConfiguration.TestPackageFilterConfiguration(values = listOf("com.example"), subpackages = true, enabled = false).toTestFilter().filterNot(tests) shouldBeEqualTo tests
    }
}

private fun stubTest(pkg: String) = MarathonTest(pkg, "SimpleTest", "fakeMethod", emptyList())
