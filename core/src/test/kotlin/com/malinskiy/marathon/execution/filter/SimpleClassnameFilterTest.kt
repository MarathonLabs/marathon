package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.extension.toTestFilter
import org.amshove.kluent.shouldBeEqualTo
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
        TestFilterConfiguration.SimpleClassnameFilterConfiguration("""^((?!Abstract).)*Test${'$'}""".toRegex()).toTestFilter()
    val tests = listOf(
        simpleTest,
        complexTest,
        someClass
    )

    @TempDir
    lateinit var temp: File

    @Test
    fun shouldFilter() {
        simpleClassnameFilter.filter(tests) shouldBeEqualTo listOf(simpleTest, complexTest)
    }

    @Test
    fun shouldFilterNot() {
        simpleClassnameFilter.filterNot(tests) shouldBeEqualTo listOf(someClass)
    }

    @Test
    fun disabled() {
        val filter = TestFilterConfiguration.SimpleClassnameFilterConfiguration("""^((?!Abstract).)*Test${'$'}""".toRegex(), enabled = false).toTestFilter()
        filter.filterNot(tests) shouldBeEqualTo tests
        filter.filter(tests) shouldBeEqualTo tests
    }

    @Test
    fun `should throw exception when more than one parameter specified`() {
        assertThrows<ConfigurationException> {
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(
                regex = """^((?!Abstract).)*Test${'$'}""".toRegex(),
                values = listOf("SimpleTest")
            ).validate()
        }
    }

    @Test
    fun `should filter if values are specified`() {
        TestFilterConfiguration.SimpleClassnameFilterConfiguration(values = listOf("SimpleTest"))
            .toTestFilter()
            .filter(tests) shouldBeEqualTo listOf(simpleTest)
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
        TestFilterConfiguration.SimpleClassnameFilterConfiguration(file = file).toTestFilter()
            .filter(tests) shouldBeEqualTo listOf(simpleTest)
    }
}

private fun stubTest(clazz: String) = MarathonTest("com.example", clazz, "fakeMethod", emptyList())
