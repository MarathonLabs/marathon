package com.malinskiy.marathon.execution.queue

sealed class TestAction {
    /**
     * Indicates that test reached terminal state of no return according to the current execution strategy logic
     * Test outputs can be produced after this action and some logic about retries left can be executed
     */
    object Complete : TestAction()
}
