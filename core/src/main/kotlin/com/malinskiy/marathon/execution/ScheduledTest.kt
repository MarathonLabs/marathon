package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test

data class ScheduledTest(val test: Test,
                         val device: Device)