package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.assert.shouldBeEqualTo
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.test.TestCoroutineContext
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.util.concurrent.TimeUnit

class SuccessScenarios : Spek({
    given("one healthy device") {
        on("execution of one test") {
            it("should pass") {
                var output: File? = null
                val context = TestCoroutineContext("testing context")

                val marathon = setupMarathon {
                    val test = Test("test", "SimpleTest", "test", emptySet())
                    val device = StubDevice()

                    configuration {
                        output = outputDir

                        tests {
                            listOf(test)
                        }

                        vendorConfiguration.deviceProvider.coroutineContext = context

                        devices {
                            delay(1000)
                            it.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                        }
                    }

                    device.executionResults = mapOf(
                            test to arrayOf(TestStatus.PASSED)
                    )
                }

                launch(context = context) {
                    marathon.runAsync()
                }

                context.advanceTimeBy(2, TimeUnit.SECONDS)

                File(output!!.absolutePath + "/test_result", "raw.json")
                        .shouldBeEqualTo(File(javaClass.getResource("/output/raw/success_scenario_1.json").file))
            }
        }
    }
})
