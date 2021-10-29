package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import com.malinskiy.marathon.test.MetaProperty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class AnnotationDataFilterTest {
    val test1 = stubTest(AnnotationData("com.example.AnnotationOne", ".*"), AnnotationData("com.example.AnnotationTwo", ".*"))
    val test2 = stubTest(AnnotationData("com.example.AnnotationOne", ".*"))
    private val test3 = stubTest(*arrayOf<MetaProperty>())
    private val filter =
        TestFilterConfiguration.AnnotationDataFilterConfiguration("""com\.example.*""".toRegex(), ".*".toRegex()).toTestFilter()
    val tests = listOf(test1, test2, test3)

    val test4 = stubTest(
        AnnotationData("com.example.IncorrectAnnotation", "CORRECT_VALUE"),
        AnnotationData("com.example.CorrectAnnotation", "INCORRECT_VALUE")
    )
    private val filter2 = TestFilterConfiguration.AnnotationDataFilterConfiguration(
        """com\.example\.CorrectAnnotation""".toRegex(),
        "CORRECT_VALUE".toRegex()
    ).toTestFilter()

    private val test5MetaProperty = MetaProperty("com.example.CorrectAnnotation", mapOf("testKey" to "testValue"))
    private val test5 = stubTest(test5MetaProperty)

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldBeEqualTo listOf(test1, test2)
    }

    @Test
    fun shouldFilterNot() {
        filter.filterNot(tests) shouldBeEqualTo listOf(test3)
    }

    @Test
    fun shouldBeEmptyListForFilter() {
        filter2.filter(listOf(test4)) shouldBeEqualTo emptyList()
    }

    @Test
    fun shouldNotFailIfValueFieldIsNotExist() {
        filter.filter(listOf(test5)) shouldBeEqualTo emptyList()
    }
}

private class AnnotationData(
    var name: String = "",
    var value: String = ""
)


private fun stubTest(vararg annotations: MetaProperty) = MarathonTest("com.sample", "SimpleTest", "fakeMethod", listOf(*annotations))
private fun stubTest(vararg annotations: AnnotationData) =
    MarathonTest("com.sample", "SimpleTest", "fakeMethod", annotations.map { MetaProperty(it.name, mapOf("value" to it.value)) })
