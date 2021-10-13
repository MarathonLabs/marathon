package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
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

    @TempDir
    lateinit var temp: File

    @Test
    fun shouldFilter() {
        simpleClassnameFilter.filter(tests) shouldEqual listOf(simpleTest, complexTest)
    }

    @Test
    fun shouldFilterNot() {
        simpleClassnameFilter.filterNot(tests) shouldEqual listOf(someClass)
    }

    @Test
    fun `should throw exception when more than one parameter specified`() {
        assertThrows<ConfigurationException> {
            SimpleClassnameFilter(
                regex = """^((?!Abstract).)*Test${'$'}""".toRegex(),
                values = listOf("SimpleTest")
            ).validate()
        }
    }

    @Test
    fun `should filter if values are specified`() {
        SimpleClassnameFilter(values = listOf("SimpleTest")).filter(tests) shouldBeEqualTo listOf(simpleTest)
    }

    @Test
    fun `should filter if values file is specified`() {
        val file = File(temp, "testfile").apply {
            writeText(
                """
                SimpleTest
            """.trimIndent()
            )
        }
        SimpleClassnameFilter(file = file).filter(tests) shouldBeEqualTo listOf(simpleTest)
    }
}

private fun stubTest(clazz: String) = MarathonTest("com.example", clazz, "fakeMethod", emptyList())
