package com.malinskiy.marathon.execution.filter

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class FullyQualifiedTestnameFilterTest {
    private val test1 = MarathonTest("com.example", "Test1", "test1", emptyList())
    private val test2 = MarathonTest("com.example.subpackage", "Test2", "test2", emptyList())
    private val test3 = MarathonTest("com.sample", "Test3", "test3", emptyList())
    private val test4 = MarathonTest("", "Test4", "test1", emptyList())

    private val filter = FullyQualifiedTestnameFilter("""com\.example\..*#test.*""".toRegex())
    val tests = listOf(
        test1,
        test2,
        test3,
        test4
    )

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldBeEqualTo listOf(test1, test2)
    }

    @Test
    fun shouldNotFilter() {
        filter.filterNot(tests) shouldBeEqualTo listOf(test3, test4)
    }

    @Test
    fun shouldFilterWithValues() {
        FullyQualifiedTestnameFilter(
            values = listOf(
                "com.example.subpackage.Test2#test2",
                "Test4#test1"
            )
        ).filter(tests) shouldBeEqualTo listOf(test2, test4)
    }
}
