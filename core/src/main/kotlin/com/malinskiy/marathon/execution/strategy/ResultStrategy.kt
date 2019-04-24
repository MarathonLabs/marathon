package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState

interface ResultStrategy {
    fun createStateMachine(initialCount: Int, onTransitionHandler: (StateMachine.Transition<TestState, TestEvent, TestAction>) -> Unit): StateMachine<TestState, TestEvent, TestAction>
}