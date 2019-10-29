package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object FullyQualifiedClassnameFilterSpec : Spek(
    {
        val simpleTest = stubTest("com.example")
        val complexTest = stubTest("com.example.subpackage")
        val someClass = stubTest("com.sample")


        given("a FQ classname filter") {
            val filter = FullyQualifiedClassnameFilter("""com\.example\.ClassTest""".toRegex())

            on("a bunch of tests") {
                val tests = listOf(
                    simpleTest,
                    complexTest,
                    someClass
                )
                it("should filter properly") {
                    filter.filter(tests) shouldEqual listOf(simpleTest)
                }
                it("should filterNot properly") {
                    filter.filterNot(tests) shouldEqual listOf(complexTest, someClass)
                }
            }
        }
    })

private fun stubTest(pkg: String) = Test(pkg, "ClassTest", "fakeMethod", emptyList())
