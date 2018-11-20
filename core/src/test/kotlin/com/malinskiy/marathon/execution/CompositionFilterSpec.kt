package com.malinskiy.marathon.execution

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
                arrayOf(
                        SimpleClassnameFilter("Cat".toRegex()),
                        AnnotationFilter("""com\.example\.BestAnimal.*""".toRegex())),
                CompositionFilter.OPERATION.UNION)

        on("a bunch of tests") {
            val tests = listOf(
                    dogTest,
                    catTest,
                    horseTest
            )
            it("should filter properly the union") {
                filterUnion.filter(tests) shouldEqual listOf(dogTest, catTest)
            }
            it("should filterNot properly the union") {
                filterUnion.filterNot(tests) shouldEqual listOf(horseTest)
            }
        }
    }

    given("a CompositionFilter with different Filters and Intersection Operation") {
        val filterUnion = CompositionFilter(
                arrayOf(
                        SimpleClassnameFilter("Dog".toRegex()),
                        SimpleClassnameFilter("Cat".toRegex())),
                CompositionFilter.OPERATION.INTERSECTION)

        on("a bunch of tests") {
            val tests = listOf(
                    dogTest,
                    catTest,
                    horseTest
            )
            it("should filter properly the union") {
                filterUnion.filter(tests) shouldEqual emptyList()
            }
            it("should filterNot properly the union") {
                filterUnion.filterNot(tests) shouldEqual listOf(dogTest, catTest, horseTest)
            }
        }
    }


})

private fun stubTest(className: String,
                     vararg annotations: String) = Test("com.example", className, "fakeMethod", listOf(*annotations))
