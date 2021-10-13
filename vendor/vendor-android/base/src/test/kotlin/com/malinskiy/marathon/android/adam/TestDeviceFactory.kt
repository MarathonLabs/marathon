package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.android.AndroidConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.time.SystemTimer
import java.time.Clock

object TestDeviceFactory {
    fun create(client: AndroidDebugBridgeClient, configuration: Configuration, logcatManager: LogcatManager): AndroidDevice {
        return AdamAndroidDevice(
            client = client,
            deviceStateTracker = DeviceStateTracker(),
            logcatManager = logcatManager,
            AndroidTestBundleIdentifier(),
            "emulator-5554",
            configuration,
            configuration.vendorConfiguration as AndroidConfiguration,
            Track(),
            SystemTimer(Clock.systemDefaultZone()),
            SerialStrategy.AUTOMATIC
        )
    }
}
