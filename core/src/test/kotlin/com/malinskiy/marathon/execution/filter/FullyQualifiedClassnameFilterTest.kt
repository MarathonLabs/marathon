package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class FullyQualifiedClassnameFilterTest {
    private val simpleTest = stubTest("com.example")
    private val complexTest = stubTest("com.example.subpackage")
    private val someClass = stubTest("com.sample")

    private val filter =
        TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration("""com\.example\.ClassTest""".toRegex()).toTestFilter()
    val tests = listOf(
        simpleTest,
        complexTest,
        someClass
    )

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldBeEqualTo listOf(simpleTest)
    }

    @Test
    fun shouldNotFilter() {
        filter.filterNot(tests) shouldBeEqualTo listOf(complexTest, someClass)
    }
    @Test
    fun disabled() {
        val filter = TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration("""com\.example\.ClassTest""".toRegex(), enabled = false).toTestFilter()
        filter.filterNot(tests) shouldBeEqualTo tests
        filter.filter(tests) shouldBeEqualTo tests
    }

}

private fun stubTest(pkg: String) = MarathonTest(pkg, "ClassTest", "fakeMethod", emptyList())
