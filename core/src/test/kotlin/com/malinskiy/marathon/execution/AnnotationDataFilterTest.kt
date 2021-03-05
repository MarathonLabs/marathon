package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.MetaProperty
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class AnnotationDataFilterTest {
    val test1 = stubTest(AnnotationData("com.example.AnnotationOne", ".*"), AnnotationData("com.example.AnnotationTwo", ".*"))
    val test2 = stubTest(AnnotationData("com.example.AnnotationOne", ".*"))
    private val test3 = stubTest(*arrayOf<MetaProperty>())
    private val filter = AnnotationDataFilter("""com\.example.*""".toRegex(), ".*".toRegex())
    val tests = listOf(test1, test2, test3)

    val test4 = stubTest(AnnotationData("com.example.IncorrectAnnotation", "CORRECT_VALUE"),
                         AnnotationData("com.example.CorrectAnnotation", "INCORRECT_VALUE"))
    private val filter2 = AnnotationDataFilter("""com\.example\.CorrectAnnotation""".toRegex(), "CORRECT_VALUE".toRegex())

    @Test
    fun shouldFilter() {
        filter.filter(tests) shouldEqual listOf(test1, test2)
    }

    @Test
    fun shouldFilterNot() {
        filter.filterNot(tests) shouldEqual listOf(test3)
    }

    @Test
    fun shouldBeEmptyListForFilter() {
        filter2.filter(listOf(test4)) shouldEqual emptyList()
    }
}

private class AnnotationData(
    var name: String = "",
    var value: String = ""
)


private fun stubTest(vararg annotations: MetaProperty) = MarathonTest("com.sample", "SimpleTest", "fakeMethod", listOf(*annotations))
private fun stubTest(vararg annotations: AnnotationData) =
    MarathonTest("com.sample", "SimpleTest", "fakeMethod", annotations.map { MetaProperty(it.name, mapOf("value" to it.value)) })
