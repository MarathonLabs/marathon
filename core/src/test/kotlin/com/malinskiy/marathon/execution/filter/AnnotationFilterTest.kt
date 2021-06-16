package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.test.MetaProperty
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class AnnotationFilterTest {
    val test1 = stubTest("com.example.AnnotationOne", "com.sample.AnnotationTwo")
    val test2 = stubTest("com.example.AnnotationOne")
    private val test3 = stubTest(*arrayOf<MetaProperty>())
    private val filter = AnnotationFilter(regex = """com\.example.*""".toRegex())
    val tests = listOf(test1, test2, test3)

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldEqual listOf(test1, test2)
    }

    @Test
    fun shouldFilterNot() {
        filter.filterNot(tests) shouldEqual listOf(test3)
    }
}


private fun stubTest(vararg annotations: MetaProperty) = MarathonTest("com.sample", "SimpleTest", "fakeMethod", listOf(*annotations))
private fun stubTest(vararg annotations: String) =
    MarathonTest("com.sample", "SimpleTest", "fakeMethod", annotations.map { MetaProperty(it) })
