package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import com.malinskiy.marathon.test.MetaProperty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class CompositionFilterTest {
    private val dogTest = stubTest("FilterAnimalDogTest", "com.example.BestAnimal")
    private val catTest = stubTest("FilterAnimalCatTest", "")
    private val horseTest = stubTest("FilterAnimalHorseTest", "")
    private val cubeTest = stubTest("FilterCubeTest", "")
    val tests = listOf(
        dogTest,
        catTest,
        horseTest
    )

    private val union = TestFilterConfiguration.CompositionFilterConfiguration(
        listOf(
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*Cat.*".toRegex(),),
            TestFilterConfiguration.AnnotationFilterConfiguration("com.example.BestAnimal".toRegex())
        ),
        TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.UNION
    ).toTestFilter()

    private val intersection = TestFilterConfiguration.CompositionFilterConfiguration(
        listOf(
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*Dog.*".toRegex(),),
            TestFilterConfiguration.AnnotationFilterConfiguration("com.example.BestAnimal".toRegex())
        ),
        TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.INTERSECTION
    ).toTestFilter()

    private val subtraction = TestFilterConfiguration.CompositionFilterConfiguration(
        listOf(
            TestFilterConfiguration.SimpleClassnameFilterConfiguration(".*Animal.*".toRegex(),),
            TestFilterConfiguration.AnnotationFilterConfiguration("com.example.BestAnimal".toRegex())
        ),
        TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.SUBTRACT
    ).toTestFilter()

    @Test
    fun shouldFilterUnion() {
        union.filter(tests) shouldBeEqualTo listOf(catTest, dogTest)
    }

    @Test
    fun shouldFilterNotUnion() {
        union.filterNot(tests) shouldBeEqualTo listOf(horseTest)
    }

    @Test
    fun shouldFilterIntersection() {
        intersection.filter(tests) shouldBeEqualTo listOf(dogTest)
    }

    @Test
    fun shouldFilterNotIntersection() {
        intersection.filterNot(tests) shouldBeEqualTo listOf(catTest, horseTest)
    }

    @Test
    fun shouldFilterSubtraction() {
        subtraction.filter(tests + cubeTest) shouldBeEqualTo listOf(catTest, horseTest)
    }

    @Test
    fun shouldFilterNotSubtraction() {
        subtraction.filterNot(tests + cubeTest) shouldBeEqualTo listOf(dogTest, cubeTest)
    }
}

private fun stubTest(className: String, vararg annotations: String) =
    MarathonTest("com.sample", className, "fakeMethod", annotations.map { MetaProperty(it) })
