package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object TestPackageFilterSpec: Spek({
    val simpleTest = stubTest("com.example")
    val complexTest = stubTest("com.example.subpackage")
    val someClass = stubTest("com.sample")


    given("a simple classname filter") {
        val simpleClassnameFilter = TestPackageFilter("""com\.example.*""".toRegex())

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

private fun stubTest(pkg: String) = Test(pkg, "SimpleTest", "fakeMethod", emptyList())
