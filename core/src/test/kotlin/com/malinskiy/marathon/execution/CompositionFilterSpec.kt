package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object CompositionFilterSpec : Spek({
    val dogTest = stubTest("FilterAnimalDogTest", "com.example.BestAnimal")
    val catTest = stubTest("FilterAnimalCatTest", "")
    val horseTest = stubTest("FilterAnimalHorseTest", "")


    given("a CompositionFilter with different Filters and Union Operation") {
        val filterUnion = CompositionFilter(
                listOf(
                        SimpleClassnameFilter(".*Cat.*".toRegex()),
                        AnnotationFilter("com.example.BestAnimal".toRegex())),
                CompositionFilter.OPERATION.UNION)

        on("a bunch of tests") {
            val tests = listOf(
                    dogTest,
                    catTest,
                    horseTest
            )
            it("should filter properly the union") {
                filterUnion.filter(tests) shouldEqual listOf(catTest, dogTest)
            }
            it("should filterNot properly the union") {
                filterUnion.filterNot(tests) shouldEqual listOf(horseTest)
            }
        }
    }

    given("a CompositionFilter with different Filters and Intersection Operation") {
        val filterIntersection = CompositionFilter(
                listOf(
                        SimpleClassnameFilter(".*Dog.*".toRegex()),
                        AnnotationFilter("com.example.BestAnimal".toRegex())),
                CompositionFilter.OPERATION.INTERSECTION)

        on("a bunch of tests") {
            val tests = listOf(
                    dogTest,
                    catTest,
                    horseTest
            )
            it("should filter properly the intersection") {
                filterIntersection.filter(tests) shouldEqual listOf(dogTest)
            }
            it("should filterNot properly the intersection") {
                filterIntersection.filterNot(tests) shouldEqual listOf(catTest, horseTest)
            }
        }
    }

    given("a CompositionFilter with different Filters and Subtract Operation") {
        val filterIntersection = CompositionFilter(
                listOf(
                        SimpleClassnameFilter(".*Dog.*".toRegex()),
                        AnnotationFilter("com.example.BestAnimal".toRegex())),
                CompositionFilter.OPERATION.SUBTRACT)

        on("a bunch of tests") {
            val tests = listOf(
                    dogTest,
                    catTest,
                    horseTest
            )
            it("should filter properly the subtract") {
                filterIntersection.filter(tests) shouldEqual listOf(catTest, horseTest)
            }
            it("should filterNot properly the subtract") {
                filterIntersection.filterNot(tests) shouldEqual listOf(dogTest)
            }
        }
    }


})

private fun stubTest(className: String, vararg annotations: MetaProperty) =
        Test("com.example", className, "fakeMethod", listOf(*annotations))
private fun stubTest(className: String, vararg annotations: String) =
        Test("com.sample", className, "fakeMethod", annotations.map { MetaProperty(it) })
