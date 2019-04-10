package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.StateMachine

interface TestResultStateMachineFactory {
    fun createStateMachine(initialCount: Int, onTransitionHandler: (StateMachine.Transition<TestState, TestEvent, TestAction>) -> Unit): StateMachine<TestState, TestEvent, TestAction>
}