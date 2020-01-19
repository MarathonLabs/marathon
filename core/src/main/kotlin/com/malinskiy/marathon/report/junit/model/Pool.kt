package com.malinskiy.marathon.report.junit.model

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId

data class Pool(
    val devicePoolId: DevicePoolId,
    val deviceInfo: DeviceInfo
)
