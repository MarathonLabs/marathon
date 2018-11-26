package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.experimental.delay
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class SuccessScenarios : Spek({
    given("one healthy device") {
        on("execution of one test") {
            it("should pass") {
                val marathon = setupMarathon {
                    val test = Test("test", "SimpleTest", "test", emptySet())
                    val device = StubDevice()

                    configuration = Mocks.Configuration.DEFAULT
                    tests = listOf(test)
                    provideDevices {
                        delay(1000)
                        it.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                    }
                    device.executionResults = mapOf(
                            test to arrayOf(TestStatus.PASSED)
                    )
                }

                marathon.run()
            }
        }
    }
})
