package com.malinskiy.marathon.ios.test

import com.malinskiy.marathon.test.Test

sealed class TestEvent

object TestRunStartedEvent : TestEvent()
data class TestStarted(val id: Test) : TestEvent()
data class TestFailed(val id: Test, val startTime: Long, val endTime: Long) : TestEvent()
data class TestEnded(val id: Test, val startTime: Long, val endTime: Long) : TestEvent()
object TestRunEnded : TestEvent()
