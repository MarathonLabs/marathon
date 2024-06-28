package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import com.malinskiy.marathon.test.MetaProperty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class AnnotationFilterTest {
    val test1 = stubTest("com.example.AnnotationOne", "com.sample.AnnotationTwo")
    val test2 = stubTest("com.example.AnnotationOne")
    private val test3 = stubTest(*arrayOf<MetaProperty>())
    private val filter = TestFilterConfiguration.AnnotationFilterConfiguration(regex = """com\.example.*""".toRegex()).toTestFilter()
    val tests = listOf(test1, test2, test3)

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldBeEqualTo listOf(test1, test2)
    }

    @Test
    fun shouldFilterNot() {
        filter.filterNot(tests) shouldBeEqualTo listOf(test3)
    }

    @Test
    fun disabled() {
        val filter = TestFilterConfiguration.AnnotationFilterConfiguration(regex = """com\.example.*""".toRegex(), enabled = false).toTestFilter()
        filter.filterNot(tests) shouldBeEqualTo tests
        filter.filter(tests) shouldBeEqualTo tests
    }
}


private fun stubTest(vararg annotations: MetaProperty) = MarathonTest("com.sample", "SimpleTest", "fakeMethod", listOf(*annotations))
private fun stubTest(vararg annotations: String) =
    MarathonTest("com.sample", "SimpleTest", "fakeMethod", annotations.map { MetaProperty(it) })
