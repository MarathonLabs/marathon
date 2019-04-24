package com.malinskiy.marathon.execution.strategy.impl.result

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState
import com.malinskiy.marathon.execution.strategy.ResultStrategy

class LenientTestResultStateMachineFactory : ResultStrategy {
    override fun createStateMachine(initialCount: Int, onTransitionHandler: (StateMachine.Transition<TestState, TestEvent, TestAction>) -> Unit): StateMachine<TestState, TestEvent, TestAction> {
        return StateMachine.create {
            initialState(TestState.Added(initialCount))
            state<TestState.Added> {
                on<TestEvent.Passed> {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
                on<TestEvent.Failed> {
                    when (this.count > 1) {
                        true -> transitionTo(TestState.Executed(it.device, it.testResult, this.count - 1))
                        false -> transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                    }
                }
                on<TestEvent.Remove> {
                    transitionTo(this.copy(count = this.count - it.diff))
                }
            }
            state<TestState.Executed> {
                on<TestEvent.Failed> {
                    when (this.count > 1) {
                        true -> transitionTo(this.copy(device = it.device, testResult = it.testResult, count = this.count - 1))
                        false -> transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                    }
                }
                on<TestEvent.Remove> {
                    transitionTo(this.copy(count = this.count - it.diff))
                }
                on<TestEvent.Passed> {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
                on<TestEvent.Retry> {
                    transitionTo(this.copy(count = this.count + 1))
                }
            }
            state<TestState.Failed> {
            }
            state<TestState.Passed> {
                on<TestEvent.Failed> {
                    dontTransition()
                }
            }
            onTransition(onTransitionHandler)
        }
    }
}