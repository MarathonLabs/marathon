package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class TestPackageFilterTest {
    private val simpleTest = stubTest("com.example")
    private val complexTest = stubTest("com.example.subpackage")
    private val someClass = stubTest("com.sample")
    private val simpleClassnameFilter =
        TestFilterConfiguration.TestPackageFilterConfiguration(regex = """com\.example.*""".toRegex()).toTestFilter()
    val tests = listOf(
        simpleTest,
        complexTest,
        someClass
    )

    @Test
    fun shouldFilter() {
        simpleClassnameFilter.filter(tests) shouldBeEqualTo listOf(simpleTest, complexTest)
    }

    @Test
    fun shouldFilterNot() {
        simpleClassnameFilter.filterNot(tests) shouldBeEqualTo listOf(someClass)
    }
}

private fun stubTest(pkg: String) = MarathonTest(pkg, "SimpleTest", "fakeMethod", emptyList())
