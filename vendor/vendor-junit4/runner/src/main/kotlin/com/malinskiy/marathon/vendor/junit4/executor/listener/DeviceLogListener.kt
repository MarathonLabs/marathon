package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.vendor.junit4.Junit4Device

class DeviceLogListener(
    private val device: Junit4Device,
    private val devicePoolId: DevicePoolId,
    private val logWriter: LogWriter,
) : LineListener {
    override fun onLine(line: String) {
        logWriter.appendLogs(devicePoolId, device.toDeviceInfo(), line)
    }
}
