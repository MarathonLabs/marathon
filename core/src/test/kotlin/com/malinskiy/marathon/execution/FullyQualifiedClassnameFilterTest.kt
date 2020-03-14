package com.malinskiy.marathon.execution

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class FullyQualifiedClassnameFilterTest {
    private val simpleTest = stubTest("com.example")
    private val complexTest = stubTest("com.example.subpackage")
    private val someClass = stubTest("com.sample")

    private val filter = FullyQualifiedClassnameFilter("""com\.example\.ClassTest""".toRegex())
    val tests = listOf(
        simpleTest,
        complexTest,
        someClass
    )

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldEqual listOf(simpleTest)
    }

    @Test
    fun shouldNotFilter() {
        filter.filterNot(tests) shouldEqual listOf(complexTest, someClass)
    }
}

private fun stubTest(pkg: String) = MarathonTest(pkg, "ClassTest", "fakeMethod", emptyList())
