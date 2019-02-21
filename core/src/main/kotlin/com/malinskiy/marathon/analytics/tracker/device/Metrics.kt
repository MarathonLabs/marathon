package com.malinskiy.marathon.analytics.tracker.device

data class DeviceInitMetric(val serialNumber: String,
                            val startTime: Long,
                            val finishTime: Long,
                            val type: DeviceInitType)

enum class DeviceInitType {
    DEVICE_PROVIDER_INIT,
    DEVICE_PREPARE
}