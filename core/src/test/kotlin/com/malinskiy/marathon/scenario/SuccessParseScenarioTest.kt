@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.config.ParseCommand
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import com.malinskiy.marathon.test.Test as MarathonTest

class SuccessParseScenarioTest {
    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `one healthy device execution of the parse command should pass`() = runTest {
        var output: File? = null

        val marathon = setupMarathon {
            val test = MarathonTest("test", "SimpleTest", "test", emptySet())
            val device = StubDevice()

            configuration {
                output = outputDir

                tests {
                    listOf(test)
                }

                deviceProvider.context = coroutineContext

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                }
            }

            device.executionResults = mapOf(
                test to arrayOf(TestStatus.PASSED)
            )
        }

        val job = launch {
            marathon.runAsync(executionCommand = ParseCommand(outputFileName = "parse_result"))
        }

        advanceTimeBy(20_000)

        job.isCompleted shouldBe true
        val content = File(output!!.absolutePath, "parse_result.yml").readText()
        content.trimIndent() shouldBeEqualTo oneTestResult
    }
}

private val oneTestResult: String = """---
tests:
- pkg: "test"
  clazz: "SimpleTest"
  method: "test"
  metaProperties: []""".trimIndent()
