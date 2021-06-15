package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.time.SystemTimer
import java.time.Clock

object TestDeviceFactory {
    fun create(client: AndroidDebugBridgeClient, configuration: Configuration, logcatManager: LogcatManager): AndroidDevice {
        return AdamAndroidDevice(
            client = client,
            deviceStateTracker = DeviceStateTracker(),
            logcatManager = logcatManager,
            "emulator-5554",
            configuration,
            configuration.vendorConfiguration as AndroidConfiguration,
            Track(),
            SystemTimer(Clock.systemDefaultZone()),
            SerialStrategy.AUTOMATIC
        )
    }
}
