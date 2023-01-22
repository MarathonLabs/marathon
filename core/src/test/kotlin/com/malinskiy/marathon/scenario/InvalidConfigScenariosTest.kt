package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.stopKoin
import java.io.File
import java.time.Instant
import com.malinskiy.marathon.test.Test as MarathonTest

class InvalidConfigScenariosTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test()
    fun `one healthy device and invalid marathon config should fail`() {
        assertThrows<ConfigurationException> {
            runTest {
                var output: File? = null

                val marathon = setupMarathon {
                    val test = MarathonTest("test", "SimpleTest", "test", emptySet())
                    val device = StubDevice()

                    configuration {
                        output = outputDir

                        tests {
                            listOf(test)
                        }

                        flakinessStrategy = FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration(.2, 2, Instant.now())
                        shardingStrategy = ShardingStrategyConfiguration.CountShardingStrategyConfiguration(2)

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
                    marathon.runAsync()
                }

                advanceTimeBy(20_000)

                job.isCompleted shouldBe true
            }
        }
    }
}
