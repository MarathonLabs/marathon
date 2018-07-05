package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object SimpleClassnameFilterSpec: Spek({
    val simpleTest = stubTest("SimpleTest")
    val complexTest = stubTest("ComplexTest")
    val someClass = stubTest("SomeClass")


    given("a simple classname filter") {
        val simpleClassnameFilter = SimpleClassnameFilter("""^((?!Abstract).)*Test${'$'}""".toRegex())

        on("a bunch of tests") {
            val tests = listOf(
                    simpleTest,
                    complexTest,
                    someClass
            )
            it("should filter properly") {
                simpleClassnameFilter.filter(tests) shouldEqual listOf(simpleTest, complexTest)
            }

            it("should filterNot properly") {
                simpleClassnameFilter.filterNot(tests) shouldEqual listOf(someClass)
            }
        }
    }
})

private fun stubTest(clazz: String) = Test("com.example", clazz, "fakeMethod", emptyList())
