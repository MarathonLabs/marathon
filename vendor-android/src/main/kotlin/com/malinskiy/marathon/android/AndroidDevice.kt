package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.android.executor.AndroidAppInstaller
import com.malinskiy.marathon.android.executor.AndroidDeviceTestRunner
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.QueueMessage
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.util.UUID

class AndroidDevice(val ddmsDevice: IDevice) : Device {

    companion object {
        private const val JELLY_BEAN_SDK_VERSION = 16
    }

    override val model: String by lazy {
        ddmsDevice.getProperty("ro.product.model") ?: "Unknown"
    }

    override val manufacturer: String by lazy {
        ddmsDevice.getProperty("ro.product.manufacturer") ?: "Unknown"
    }

    override val deviceFeatures: Collection<DeviceFeature>
        get() {
            val videoSupport = ddmsDevice.supportsFeature(IDevice.Feature.SCREEN_RECORD) &&
                    manufacturer != "Genymotion"
            val screenshotSupport = operatingSystem.version.toInt() >= JELLY_BEAN_SDK_VERSION

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
                                 testBatch: TestBatch,
                                 tracker: Analytics,
                                 queueChannel: SendChannel<QueueMessage.FromDevice>) {
        launch(context) {
            AndroidDeviceTestRunner(this@AndroidDevice, tracker).execute(configuration, devicePoolId, testBatch, queueChannel)
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
