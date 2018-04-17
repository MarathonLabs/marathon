package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.test.TestBatch
import java.util.*

class AndroidDevice(val ddmsDevice: IDevice) : Device {
    override val model: String by lazy {
        ddmsDevice.getProperty("ro.product.model")
    }
    override val manufacturer: String by lazy {
        ddmsDevice.getProperty("ro.product.manufacturer")
    }
    override val deviceFeatures: Collection<DeviceFeature>
        get() {
            val videoSupport = ddmsDevice.supportsFeature(IDevice.Feature.SCREEN_RECORD) &&
                    manufacturer != "Genymotion"
            val screenshotSupport = operatingSystem.version.toInt() >= 16

            val features = mutableListOf<DeviceFeature>()

            if (videoSupport) features.add(DeviceFeature.VIDEO)
            if (screenshotSupport) features.add(DeviceFeature.SCREENSHOT)

            return features
        }
    override val serialNumber: String by lazy {
        val serialNumber: String? = ddmsDevice.getProperty("ro.boot.serialno")
        if (!serialNumber.isNullOrEmpty()) serialNumber

        val hostName: String? = ddmsDevice.getProperty("net.hostname")
        if (!hostName.isNullOrEmpty()) hostName

        UUID.randomUUID().toString()
    }
    override val operatingSystem: OperatingSystem
        get() = OperatingSystem(ddmsDevice.getProperty("ro.build.version.sdk") ?: "unknown")
    override val networkState: NetworkState
        get() = when (ddmsDevice.isOnline) {
            true -> NetworkState.CONNECTED
            else -> NetworkState.DISCONNECTED
        }
    override val healthy: Boolean
        get() = when (ddmsDevice.state) {
            IDevice.DeviceState.ONLINE -> true
            else -> false
        }

    override fun execute(testBatch: TestBatch) {
        val runner = RemoteAndroidTestRunner(
                instrumentationInfo.instrumentationPackage,
                instrumentationInfo.testRunnerClass,
                ddmsDevice)
    }


}