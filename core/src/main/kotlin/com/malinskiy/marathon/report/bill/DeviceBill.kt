package com.malinskiy.marathon.report.bill

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import java.time.Duration
import java.time.Instant

data class DeviceBill(
    val device: DeviceInfo,
    val pool: DevicePoolId,
    val start: Instant,
    val end: Instant,
    val duration: Duration,
)
