package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.test.MetaProperty
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class CompositionFilterTest {
    private val dogTest = stubTest("FilterAnimalDogTest", "com.example.BestAnimal")
    private val catTest = stubTest("FilterAnimalCatTest", "")
    private val horseTest = stubTest("FilterAnimalHorseTest", "")
    val tests = listOf(
        dogTest,
        catTest,
        horseTest
    )

    private val union = CompositionFilter(
        listOf(
            SimpleClassnameFilter(".*Cat.*".toRegex()),
            AnnotationFilter("com.example.BestAnimal".toRegex())
        ),
        CompositionFilter.OPERATION.UNION
    )

    private val intersection = CompositionFilter(
        listOf(
            SimpleClassnameFilter(".*Dog.*".toRegex()),
            AnnotationFilter("com.example.BestAnimal".toRegex())
        ),
        CompositionFilter.OPERATION.INTERSECTION
    )

    private val composition = CompositionFilter(
        listOf(
            SimpleClassnameFilter(".*Dog.*".toRegex()),
            AnnotationFilter("com.example.BestAnimal".toRegex())
        ),
        CompositionFilter.OPERATION.SUBTRACT
    )

    @Test
    fun shouldFilterUnion() {
        union.filter(tests) shouldEqual listOf(catTest, dogTest)
    }

    @Test
    fun shouldFilterNotUnion() {
        union.filterNot(tests) shouldEqual listOf(horseTest)
    }

    @Test
    fun shouldFilterIntersection() {
        intersection.filter(tests) shouldEqual listOf(dogTest)
    }

    @Test
    fun shouldFilterNotIntersection() {
        intersection.filterNot(tests) shouldEqual listOf(catTest, horseTest)
    }

    @Test
    fun shouldFilterComposition() {
        composition.filter(tests) shouldEqual listOf(catTest, horseTest)
    }

    @Test
    fun shouldFilterNotComposition() {
        composition.filterNot(tests) shouldEqual listOf(dogTest)
    }
}

private fun stubTest(className: String, vararg annotations: String) =
    MarathonTest("com.sample", className, "fakeMethod", annotations.map { MetaProperty(it) })
