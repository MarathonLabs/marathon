package com.malinskiy.marathon.ios.test

import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.test.Test

sealed class TestEvent

object TestRunStartedEvent : TestEvent()
data class TestStarted(val id: Test) : TestEvent()
data class TestFailed(val id: Test, val startTime: Long, val endTime: Long, val trace: String?) : TestEvent()
data class TestPassed(val id: Test, val startTime: Long, val endTime: Long) : TestEvent()
data class TestIgnored(val id: Test, val startTime: Long, val endTime: Long) : TestEvent()
data class TestRunFailed(val message: String, val reason: DeviceFailureReason): TestEvent()
object TestRunEnded : TestEvent()
