package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.OutputPrinter
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineContext
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.TimeUnit

object TestCountScenarios : Spek({
    given("a valid configuration") {
        on("test count request") {
            it("should output accurate count") {
                var printer = object : OutputPrinter {
                    var printedTestCount: Int? = null
                    override fun print(testCount: Int) {
                        printedTestCount = testCount
                    }
                }
                val context = TestCoroutineContext("testing context")

                val marathon = setupMarathon {
                    val test = Test("test", "SimpleTest", "test", emptySet())
                    val device = StubDevice()

                    configuration {
                        tests {
                            listOf(test)
                        }

                        vendorConfiguration.deviceProvider.context = context
                    }

                    device.executionResults = mapOf(
                            test to arrayOf(TestStatus.PASSED)
                    )
                }
                
                val job = GlobalScope.launch(context = context) {
                    marathon.runAsync(printTestCountAndExit = true, outputPrinter = printer)
                }

                context.advanceTimeBy(1, TimeUnit.SECONDS)

                job.isCompleted shouldBe true
                printer.printedTestCount shouldBe 1
            }
        }
    }
})
