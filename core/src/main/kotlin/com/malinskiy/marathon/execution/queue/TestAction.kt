package com.malinskiy.marathon.execution.queue

sealed class TestAction {
    object Conclude : TestAction()
}
