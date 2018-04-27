package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.TestBatch

data class ScheduledBatch(
    val batch: TestBatch,
    val device: Device
)