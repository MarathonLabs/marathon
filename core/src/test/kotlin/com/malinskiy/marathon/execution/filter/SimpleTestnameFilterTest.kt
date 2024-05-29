package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class SimpleTestnameFilterTest {
    private val test1 = MarathonTest("com.example", "Test", "test1", emptyList())
    private val test2 = MarathonTest("com.example.subpackage", "Test2", "test2", emptyList())
    private val test3 = MarathonTest("com.sample", "Test3", "test3", emptyList())
    private val test4 = MarathonTest("", "Test4", "test1", emptyList())

    private val filter =
        TestFilterConfiguration.SimpleTestnameFilterConfiguration(values = listOf("Test2#test2")).toTestFilter()
    val tests = listOf(
        test1,
        test2,
        test3,
        test4
    )

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldBeEqualTo listOf(test2)
    }

    @Test
    fun shouldNotFilter() {
        filter.filterNot(tests) shouldBeEqualTo listOf(test1, test3, test4)
    }

    @Test
    fun disabled() {
        val filter = TestFilterConfiguration.SimpleTestnameFilterConfiguration(values = listOf("Test2#test2"), enabled = false).toTestFilter()
        filter.filterNot(tests) shouldBeEqualTo tests
        filter.filter(tests) shouldBeEqualTo tests
    }

    @Test
    fun shouldFilterWithRegex() {
        TestFilterConfiguration.SimpleTestnameFilterConfiguration(
            regex = ".+test3".toRegex()
        ).toTestFilter().filter(tests) shouldBeEqualTo listOf(test3)
    }
}
