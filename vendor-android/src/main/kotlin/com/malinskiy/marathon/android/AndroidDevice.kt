package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import com.android.ddmlib.TimeoutException
import com.malinskiy.marathon.android.executor.AndroidAppInstaller
import com.malinskiy.marathon.android.executor.AndroidDeviceTestRunner
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred
import java.util.UUID

class AndroidDevice(val ddmsDevice: IDevice) : Device {

    val logger = MarathonLogging.logger(AndroidDevice::class.java.simpleName)

    override val abi: String by lazy {
        ddmsDevice.getProperty("ro.product.cpu.abi") ?: "Unknown"
    }

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
            val screenshotSupport = ddmsDevice.version.isGreaterOrEqualThan(JELLY_BEAN_SDK_VERSION)

            val features = mutableListOf<DeviceFeature>()

            if (videoSupport) features.add(DeviceFeature.VIDEO)
            if (screenshotSupport) features.add(DeviceFeature.SCREENSHOT)

            return features
        }

    override val serialNumber: String by lazy {
        val serialProp: String = ddmsDevice.getProperty("ro.boot.serialno") ?: ""
        val hostName: String = ddmsDevice.getProperty("net.hostname") ?: ""
        val serialNumber = ddmsDevice.serialNumber

        serialProp.takeIf { it.isNotEmpty() }
                ?: hostName.takeIf { it.isNotEmpty() }
                ?: serialNumber.takeIf { it.isNotEmpty() }
                ?: UUID.randomUUID().toString()
    }

    override val operatingSystem: OperatingSystem by lazy {
        OperatingSystem(ddmsDevice.version.apiString)
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

    override fun execute(configuration: Configuration,
                         devicePoolId: DevicePoolId,
                         testBatch: TestBatch,
                         deferred: CompletableDeferred<TestBatchResults>,
                         progressReporter: ProgressReporter) {
        AndroidDeviceTestRunner(this@AndroidDevice).execute(configuration, devicePoolId, testBatch, deferred, progressReporter)
    }

    override fun prepare(configuration: Configuration) {
        if (!waitForBoot()) throw TimeoutException("Timeout waiting for device $serialNumber to boot")

        AndroidAppInstaller(configuration).prepareInstallation(this)
        RemoteFileManager.removeRemoteDirectory(ddmsDevice)
        RemoteFileManager.createRemoteDirectory(ddmsDevice)
        clearLogcat(ddmsDevice)
    }

    private fun waitForBoot(): Boolean {
        var booted = false
        for (i in 1..30) {
            if (ddmsDevice.getProperty("sys.boot_completed") == null) {
                Thread.sleep(1000)
                logger.debug { "Device $serialNumber is still booting..." }
            } else {
                logger.debug { "Device $serialNumber booted!" }
                booted = true
                break
            }

            if (Thread.interrupted()) return true
        }

        return booted
    }

    override fun dispose() {}

    private fun clearLogcat(device: IDevice) {
        val logger = MarathonLogging.logger("AndroidDevice.clearLogcat")
        try {
            device.safeExecuteShellCommand("logcat -c", NullOutputReceiver())
        } catch (e: Throwable) {
            logger.warn("Could not clear logcat on device: ${device.serialNumber}", e)
        }
    }

    override fun toString(): String {
        return "AndroidDevice(model=$model, serial=$serialNumber)"
    }
}
