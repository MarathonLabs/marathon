package com.malinskiy.marathon.execution.strategy.impl.result

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState
import com.malinskiy.marathon.execution.strategy.ResultStrategy


class StrictTestResultStateMachineFactory : ResultStrategy {
    override fun createStateMachine(initialCount: Int,
                                    onTransitionHandler: (StateMachine.Transition<TestState, TestEvent, TestAction>) -> Unit): StateMachine<TestState, TestEvent, TestAction> {
        return StateMachine.create {
            initialState(TestState.Added(initialCount))
            state<TestState.Added> {
                on<TestEvent.Passed> {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
                on<TestEvent.Failed> {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
                on<TestEvent.Remove> {
                    transitionTo(this.copy(count = this.count - it.diff))
                }
            }
            state<TestState.Failed> {
            }
            state<TestState.Passed> {
                on<TestEvent.Failed> {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
            }
            onTransition(onTransitionHandler)
        }
    }
}
