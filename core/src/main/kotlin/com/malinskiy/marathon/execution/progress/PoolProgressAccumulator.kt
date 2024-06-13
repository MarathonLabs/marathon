package com.malinskiy.marathon.execution.progress

import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.percentage
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.spinner
import com.github.ajalt.mordant.widgets.progress.timeElapsed
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.strategy.ExecutionMode
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PoolProgressAccumulator(
    private val poolId: DevicePoolId,
    shard: TestShard,
    configuration: Configuration,
    private val track: Track
) {
    private val tests: HashMap<String, TestExecutionData> = HashMap()
    private val logger = MarathonLogging.logger {}
    private val executionStrategy = configuration.executionStrategy
    private val terminal = Terminal()
    private val progressBar = progressBarLayout {
        percentage()
        progressBar()
        completed(style = terminal.theme.success)
        spinner(Spinner.Dots())
        timeElapsed(style = terminal.theme.info)
    }.animateOnThread(terminal)

    private fun createState(initialCount: Int) = StateMachine.create<TestState, TestEvent, TestAction> {
        initialState(TestState.Added(initialCount))
        state<TestState.Added> {
            on<TestEvent.Started> {
                transitionTo(TestState.Added(total, running + 1))
            }
            on<TestEvent.Passed> {
                when (executionStrategy.mode) {
                    ExecutionMode.ANY_SUCCESS -> {
                        if (executionStrategy.fast || total <= 1) {
                            transitionTo(TestState.Passed(total = total, done = 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Passing(total = total, done = 1, running = running - 1))
                        }
                    }

                    ExecutionMode.ALL_SUCCESS -> {
                        if (total <= 1) {
                            transitionTo(TestState.Passed(total = total, done = 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Passing(total = total, done = 1, running = running - 1))
                        }
                    }
                }
            }
            on<TestEvent.Failed> {
                when (executionStrategy.mode) {
                    ExecutionMode.ANY_SUCCESS -> {
                        if (total <= 1) {
                            transitionTo(TestState.Failed(total = total, done = 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Failing(total = total, done = 1, running = running - 1))
                        }
                    }

                    ExecutionMode.ALL_SUCCESS -> {
                        if (executionStrategy.fast || total <= 1) {
                            transitionTo(TestState.Failed(total = total, done = 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Failing(total = total, done = 1, running = running - 1))
                        }
                    }
                }
            }
            on<TestEvent.Incomplete> {
                if (it.final) {
                    transitionTo(TestState.Failed(total = total, done = 0, running = running - 1), TestAction.Complete)
                } else {
                    transitionTo(TestState.Added(total = total, running = running - 1))
                }
            }
            on<TestEvent.AddRetry> {
                transitionTo(TestState.Added(total = total + 1, running = running))
            }
            on<TestEvent.RemoveAttempts> {
                transitionTo(TestState.Added(total = total - it.count, running = running))
            }
        }
        state<TestState.Passing> {
            on<TestEvent.Started> {
                transitionTo(TestState.Passing(total = total, running = running + 1, done = done))
            }
            on<TestEvent.Passed> {
                when (executionStrategy.mode) {
                    ExecutionMode.ANY_SUCCESS -> {
                        if (executionStrategy.fast || done + 1 >= total) {
                            transitionTo(TestState.Passed(total = total, running = running - 1, done = done + 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Passing(total = total, running = running - 1, done = done + 1))
                        }
                    }

                    ExecutionMode.ALL_SUCCESS -> {
                        if (total <= done + 1) {
                            transitionTo(TestState.Passed(total = total, running = running - 1, done = done + 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Passing(total = total, running = running - 1, done = done + 1))
                        }
                    }
                }
            }
            on<TestEvent.Failed> {
                when (executionStrategy.mode) {
                    ExecutionMode.ANY_SUCCESS -> {
                        if (executionStrategy.fast || done + 1 >= total) {
                            transitionTo(TestState.Passed(total = total, done = done + 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Passing(total = total, done = done + 1, running = running - 1))
                        }
                    }

                    ExecutionMode.ALL_SUCCESS -> {
                        if (executionStrategy.fast || done + 1 >= total) {
                            transitionTo(TestState.Failed(total = total, done = done + 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Failing(total = total, done = done + 1, running = running - 1))
                        }
                    }
                }
            }
            on<TestEvent.Incomplete> {
                if (it.final) {
                    when (executionStrategy.mode) {
                        ExecutionMode.ANY_SUCCESS -> {
                            transitionTo(TestState.Passed(total = total, done = done, running = running - 1), TestAction.Complete)
                        }

                        ExecutionMode.ALL_SUCCESS -> {
                            transitionTo(TestState.Failed(total = total, done = done, running = running - 1), TestAction.Complete)
                        }
                    }
                } else {
                    transitionTo(TestState.Passing(total = total, done = done, running = running - 1))
                }
            }
            on<TestEvent.RemoveAttempts> {
                transitionTo(TestState.Passing(total = total - it.count, running = running, done = done))
            }
            on<TestEvent.AddRetry> {
                transitionTo(TestState.Passing(total = total + 1, running = running, done = done))
            }
        }
        state<TestState.Failing> {
            on<TestEvent.Started> {
                transitionTo(TestState.Failing(total = total, running = running + 1, done = done))
            }
            on<TestEvent.Passed> {
                when (executionStrategy.mode) {
                    ExecutionMode.ANY_SUCCESS -> {
                        if (executionStrategy.fast || done + 1 >= total) {
                            transitionTo(TestState.Passed(total = total, done = done + 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Passing(total = total, done = done + 1, running = running - 1))
                        }
                    }

                    ExecutionMode.ALL_SUCCESS -> {
                        if (executionStrategy.fast || done + 1 >= total) {
                            transitionTo(TestState.Failed(total = total, done = done + 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Failing(total = total, done = done + 1, running = running - 1))
                        }
                    }
                }
            }
            on<TestEvent.Failed> {
                when (executionStrategy.mode) {
                    ExecutionMode.ANY_SUCCESS -> {
                        if (done + 1 >= total) {
                            transitionTo(TestState.Failed(total = total, done = done + 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Failing(total = total, done = done + 1, running = running - 1))
                        }
                    }

                    ExecutionMode.ALL_SUCCESS -> {
                        if (executionStrategy.fast || done + 1 >= total) {
                            transitionTo(TestState.Failed(total = total, done = done + 1, running = running - 1), TestAction.Complete)
                        } else {
                            transitionTo(TestState.Failing(total = total, done = done + 1, running = running - 1))
                        }
                    }
                }
            }
            on<TestEvent.Incomplete> {
                if (it.final) {
                    transitionTo(TestState.Failed(total = total, done = done, running = running - 1), TestAction.Complete)
                } else {
                    transitionTo(TestState.Failing(total = total, done = done, running = running - 1))
                }
            }
            on<TestEvent.RemoveAttempts> {
                transitionTo(TestState.Failing(total = total - it.count, done = done, running = running))
            }
            on<TestEvent.AddRetry> {
                transitionTo(TestState.Failing(total = total + 1, done = done, running = running))
            }
        }
        state<TestState.Failed> {
            on<TestEvent.Started> {
                transitionTo(TestState.Failed(total = total, running = running + 1, done = done))
            }
            on<TestEvent.Passed> {
                transitionTo(TestState.Failed(total = total, running = running - 1, done = done + 1))
            }
            on<TestEvent.Failed> {
                transitionTo(TestState.Failed(total = total, running = running - 1, done = done + 1))
            }
            on<TestEvent.Incomplete> {
                transitionTo(TestState.Failed(total = total, running = running - 1, done = done))
            }
            on<TestEvent.AddRetry> {
                transitionTo(TestState.Failed(total = total, running = running, done = done), TestAction.Complete)
            }
            on<TestEvent.RemoveAttempts> {
                transitionTo(TestState.Failed(total = total - it.count, running = running, done = done))
            }
        }
        state<TestState.Passed> {
            on<TestEvent.Started> {
                transitionTo(TestState.Passed(total = total, running = running + 1, done = done))
            }
            on<TestEvent.Passed> {
                transitionTo(TestState.Passed(total = total, running = running - 1, done = done + 1))
            }
            on<TestEvent.Failed> {
                transitionTo(TestState.Passed(total = total, running = running - 1, done = done + 1))
            }
            on<TestEvent.Incomplete> {
                transitionTo(TestState.Passed(total = total, running = running - 1, done = done))
            }
            on<TestEvent.AddRetry> {
                transitionTo(TestState.Passed(total = total, running = running, done = done), TestAction.Complete)
            }
            on<TestEvent.RemoveAttempts> {
                transitionTo(TestState.Passed(total = total - it.count, running = running, done = done))
            }
        }
        onTransition {
            if (it as? StateMachine.Transition.Valid !is StateMachine.Transition.Valid) {
                logger.error { "from ${it.fromState} event ${it.event}" }
            }
            trackTestTransition(poolId, it)
        }
    }

    init {
        val allTests = shard.tests + shard.flakyTests
        allTests.groupBy { it }.map {
            val count = it.value.size
            it.key.toTestName() to TestExecutionData(it.key, createState(count))
        }.also {
            tests.putAll(it)
        }
        progressBar.update { total = tests.size.toLong() }
        progressBar.execute()
    }

    fun testStarted(device: DeviceInfo, test: Test) {
        transition(test, TestEvent.Started)
        println("${toPercent(progress())} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} started")
    }

    /**
     * @param final used for incomplete tests to signal no more retries left, hence a decision on the status has to be made
     */
    fun testEnded(device: DeviceInfo, testResult: TestResult, final: Boolean = false): TestAction? {
        return when (testResult.status) {
            TestStatus.FAILURE -> {
                terminal.println("[${poolId.name}]-[${device.serialNumber}] ${testResult.test.toTestName()} ${TextColors.brightRed("failed")}")
                progressBar.advance()
                transition(testResult.test, TestEvent.Failed(device, testResult)).sideffect()
            }

            TestStatus.PASSED -> {
                terminal.println("[${poolId.name}]-[${device.serialNumber}] ${testResult.test.toTestName()} ${TextColors.brightGreen("passed")}")
                progressBar.advance()
                transition(testResult.test, TestEvent.Passed(device, testResult)).sideffect()
            }

            TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> {
                terminal.println("[${poolId.name}]-[${device.serialNumber}] ${testResult.test.toTestName()} ${TextColors.brightYellow("ignored")}")
                progressBar.advance()
                transition(testResult.test, TestEvent.Passed(device, testResult)).sideffect()
            }

            TestStatus.INCOMPLETE -> {
                terminal.println("[${poolId.name}]-[${device.serialNumber}] ${testResult.test.toTestName()} ${TextColors.brightBlue("incomplete")}")
                progressBar.advance()
                transition(testResult.test, TestEvent.Incomplete(device, testResult, final)).sideffect()
            }
        }
    }


    /**
     * Should always be called before testEnded, otherwise the FSM might transition into a terminal state prematurely
     */
    fun retryTest(test: Test): TestAction? {
        return transition(test, TestEvent.AddRetry).sideffect()
    }

    fun removeTest(test: Test, diff: Int): TestAction? {
        return transition(test, TestEvent.RemoveAttempts(diff)).sideffect()
    }

    private fun trackTestTransition(poolId: DevicePoolId, transition: StateMachine.Transition<TestState, TestEvent, TestAction>) {
        val validTransition = transition as? StateMachine.Transition.Valid
        val final = if (validTransition is StateMachine.Transition.Valid) {
            when (validTransition.sideEffect) {
                is TestAction.Complete -> true
                else -> false
            }
        } else false

        val (testResult: TestResult?, device: DeviceInfo?) = extractEventAndDevice(transition)
        if (testResult == null || device == null) return

        val testNameFromTestResult = testResult.test.toTestName()
        val originalTest = tests[testNameFromTestResult]?.originalTest
        val updatedTestResult = if (originalTest == null) testResult
        else testResult.copy(test = originalTest)

        track.test(poolId, device, updatedTestResult, final)
    }

    private fun extractEventAndDevice(transition: StateMachine.Transition<TestState, TestEvent, TestAction>): Pair<TestResult?, DeviceInfo?> {
        val event = transition.event
        val testResult: TestResult? = when (event) {
            is TestEvent.Passed -> event.testResult
            is TestEvent.Failed -> event.testResult
            is TestEvent.Incomplete -> {
                if (event.final) {
                    event.testResult.copy(status = TestStatus.FAILURE)
                } else {
                    event.testResult
                }
            }
            else -> null
        }
        val device: DeviceInfo? = when (event) {
            is TestEvent.Passed -> event.device
            is TestEvent.Failed -> event.device
            is TestEvent.Incomplete -> event.device
            else -> null
        }
        return Pair(testResult, device)
    }

    fun aggregateResult(): Boolean {
        synchronized(tests) {
            return tests.map {
                when (it.value.state.state) {
                    is TestState.Added -> {
                        logger.error { "Expected to run ${it.key} but no events received" }
                        false
                    }

                    is TestState.Failed -> false
                    is TestState.Failing -> {
                        logger.error { "Expected to run ${it.key} more, but no terminal events received" }
                        false
                    }

                    is TestState.Passed -> true
                    is TestState.Passing -> {
                        logger.error { "Expected to run ${it.key} more, but no terminal events received" }
                        //The test is passing but the execution mode might require all the
                        //runs to pass before considering this an actual pass
                        executionStrategy.mode == ExecutionMode.ANY_SUCCESS
                    }
                }
            }.reduce { a, b -> a && b }
        }
    }

    fun progress(): Float {
        var done = 0
        var total = 0

        tests.values.forEach {
            val state = it.state.state
            when (state) {
                is TestState.Added -> {
                    total += state.total
                }

                is TestState.Failed -> {
                    total += state.total
                    done += state.done
                }

                is TestState.Failing -> {
                    total += state.total
                    done += state.done
                }

                is TestState.Passed -> {
                    total += state.total
                    done += state.done
                }

                is TestState.Passing -> {
                    total += state.total
                    done += state.done
                }
            }
        }
        return done.toFloat() / total.toFloat()
    }

    private fun transition(test: Test, transition: TestEvent): StateMachine.Transition<TestState, TestEvent, TestAction>? {
        val testActionTransition = tests[test.toTestName()]?.state?.transition(transition)
        if (testActionTransition == null) {
            logger.warn { "No FSM registered for test ${test.toTestName()}" }
        }
        return testActionTransition
    }

    private fun toPercent(float: Float): String {
        val percent = (float * HUNDRED_PERCENT_IN_FLOAT).roundToInt()
        val format = "%02d%%"
        return String.format(format, percent)
    }

    companion object {
        const val HUNDRED_PERCENT_IN_FLOAT: Float = 100.0f
    }
}

private fun <STATE : Any, EVENT : Any, SIDE_EFFECT : Any> StateMachine.Transition<STATE, EVENT, SIDE_EFFECT>?.sideffect(): SIDE_EFFECT? {
    return when (this) {
        is StateMachine.Transition.Invalid -> null
        is StateMachine.Transition.Valid -> this.sideEffect
        null -> null
    }
}

private data class TestExecutionData(
    val originalTest: Test,
    val state: StateMachine<TestState, TestEvent, TestAction>
)
