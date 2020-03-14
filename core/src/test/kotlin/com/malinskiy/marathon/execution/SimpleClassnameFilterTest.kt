package com.malinskiy.marathon.execution

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class SimpleClassnameFilterTest {
    private val simpleTest = stubTest("SimpleTest")
    private val complexTest = stubTest("ComplexTest")
    private val someClass = stubTest("SomeClass")
    private val simpleClassnameFilter =
        SimpleClassnameFilter("""^((?!Abstract).)*Test${'$'}""".toRegex())
    val tests = listOf(
        simpleTest,
        complexTest,
        someClass
    )

    @Test
    fun shouldFilter() {
        simpleClassnameFilter.filter(tests) shouldEqual listOf(simpleTest, complexTest)
    }

    @Test
    fun shouldFilterNot() {
        simpleClassnameFilter.filterNot(tests) shouldEqual listOf(someClass)
    }
}

private fun stubTest(clazz: String) = MarathonTest("com.example", clazz, "fakeMethod", emptyList())
