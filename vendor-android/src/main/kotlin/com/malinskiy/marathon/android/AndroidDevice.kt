package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.malinskiy.marathon.android.executor.AndroidAppInstaller
import com.malinskiy.marathon.android.executor.AndroidDeviceTestRunner
import com.malinskiy.marathon.device.*
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
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
        val serialNumber: String = ddmsDevice.getProperty("ro.boot.serialno") ?: ""
        val hostName: String = ddmsDevice.getProperty("net.hostname") ?: ""

        serialNumber.takeIf { it.isNotEmpty() }
                ?: hostName.takeIf { it.isNotEmpty() }
                ?: UUID.randomUUID().toString()
    }

    override val operatingSystem: OperatingSystem by lazy {
        OperatingSystem(ddmsDevice.getProperty("ro.build.version.sdk") ?: "unknown")
    }

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

    private val context = newSingleThreadContext(this.toString())

    override suspend fun execute(configuration: Configuration,
                                 devicePoolId: DevicePoolId,
                                 testBatch: TestBatch) {
        launch(context) {
            AndroidDeviceTestRunner(this@AndroidDevice).execute(configuration, devicePoolId, testBatch)
        }.join()
    }

    override suspend fun prepare(configuration: Configuration) {
        launch(context) {
            AndroidAppInstaller(configuration).prepareInstallation(ddmsDevice)
        }.join()
    }

    override fun toString(): String {
        return "AndroidDevice(model=$model, serial=$serialNumber)"
    }
}
