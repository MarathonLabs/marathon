package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineContext
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.test.Test as MarathonTest

class InvalidConfigScenariosTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `one healthy device and invalid marathon config should fail`() {
        var output: File? = null
        val context = TestCoroutineContext("testing context")

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

                deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                }
            }

            device.executionResults = mapOf(
                test to arrayOf(TestStatus.PASSED)
            )
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(20, TimeUnit.SECONDS)

        job.isCompleted shouldBe true
        context.exceptions[0] shouldBeInstanceOf ConfigurationException::class.java
    }
}
