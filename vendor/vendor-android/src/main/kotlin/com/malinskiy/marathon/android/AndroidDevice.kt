package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.exception.InvalidSerialConfiguration
import com.malinskiy.marathon.android.executor.AndroidAppInstaller
import com.malinskiy.marathon.android.executor.AndroidDeviceTestRunner
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.android.executor.listeners.LogCatListener
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.executor.listeners.ProgressTestRunListener
import com.malinskiy.marathon.android.executor.listeners.TestRunResultsListener
import com.malinskiy.marathon.android.executor.listeners.screenshot.ScreenCapturerTestRunListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderTestRunListener
import com.malinskiy.marathon.android.serial.SerialStrategy
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.coroutines.CoroutineContext

class AndroidDevice(val ddmsDevice: IDevice,
                    private val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC) : Device, KoinComponent, CoroutineScope {

    val fileManager = RemoteFileManager(ddmsDevice)
    val track: Track by inject()

    private val dispatcher by lazy {
        newFixedThreadPoolContext(1, "AndroidDevice - execution - ${ddmsDevice.serialNumber}")
    }

    override val coroutineContext: CoroutineContext = dispatcher

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

    /**
     * We can only call this after the device finished booting
     */
    private val realSerialNumber: String by lazy {
        val marathonSerialProp: String = ddmsDevice.getProperty("marathon.serialno") ?: ""
        val serialProp: String = ddmsDevice.getProperty("ro.boot.serialno") ?: ""
        val hostName: String = ddmsDevice.getProperty("net.hostname") ?: ""
        val serialNumber = ddmsDevice.serialNumber

        val result = when (serialStrategy) {
            SerialStrategy.AUTOMATIC -> {
                marathonSerialProp.takeIf { it.isNotEmpty() }
                        ?: serialProp.takeIf { it.isNotEmpty() }
                        ?: hostName.takeIf { it.isNotEmpty() }
                        ?: serialNumber.takeIf { it.isNotEmpty() }
                        ?: UUID.randomUUID().toString()
            }
            SerialStrategy.MARATHON_PROPERTY -> marathonSerialProp
            SerialStrategy.BOOT_PROPERTY -> serialProp
            SerialStrategy.HOSTNAME -> hostName
            SerialStrategy.DDMS -> serialNumber
        }

        result.apply {
            if (this == null) throw InvalidSerialConfiguration(serialStrategy)
        }
    }

    val booted: Boolean
        get() = ddmsDevice.getProperty("sys.boot_completed") != null

    override val serialNumber: String = when {
        booted -> realSerialNumber
        else -> ddmsDevice.serialNumber
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

    override suspend fun execute(configuration: Configuration,
                                 devicePoolId: DevicePoolId,
                                 testBatch: TestBatch,
                                 deferred: CompletableDeferred<TestBatchResults>,
                                 progressReporter: ProgressReporter) {

        val deferredResult = async {
            val listeners = createListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
            AndroidDeviceTestRunner(this@AndroidDevice).execute(configuration, testBatch, listeners)
        }
        deferredResult.await()
    }

    private fun createListeners(configuration: Configuration,
                                devicePoolId: DevicePoolId,
                                testBatch: TestBatch,
                                deferred: CompletableDeferred<TestBatchResults>,
                                progressReporter: ProgressReporter): CompositeTestRunListener {
        val fileManager = FileManager(configuration.outputDir)
        val attachmentProviders = mutableListOf<AttachmentProvider>()

        val features = this.deviceFeatures

        val preferableRecorderType = configuration.vendorConfiguration.preferableRecorderType()
        val recorderListener = selectRecorderType(preferableRecorderType, features)?.let { feature ->
            prepareRecorderListener(feature, fileManager, devicePoolId, attachmentProviders)
        } ?: NoOpTestRunListener()

        val logCatListener = LogCatListener(this, devicePoolId, LogWriter(fileManager))
                .also { attachmentProviders.add(it) }

        val timer = SystemTimer()

        return CompositeTestRunListener(
                listOf(
                        recorderListener,
                        logCatListener,
                        TestRunResultsListener(testBatch, this, deferred, timer, attachmentProviders),
                        DebugTestRunListener(this),
                        ProgressTestRunListener(this, devicePoolId, progressReporter)
                )
        )
    }

    override suspend fun prepare(configuration: Configuration) {
        track.trackDevicePreparing(this) {
            val deferred = async {
                AndroidAppInstaller(configuration).prepareInstallation(this@AndroidDevice)
                fileManager.removeRemoteDirectory()
                fileManager.createRemoteDirectory()
                clearLogcat(ddmsDevice)
            }
            deferred.await()
        }
    }

    override fun dispose() {
        dispatcher.close()
    }

    private fun selectRecorderType(preferred: DeviceFeature?, features: Collection<DeviceFeature>) = when {
            features.contains(preferred) -> preferred
            features.contains(DeviceFeature.VIDEO) -> DeviceFeature.VIDEO
            features.contains(DeviceFeature.SCREENSHOT) -> DeviceFeature.SCREENSHOT
            else -> null
    }

    private fun prepareRecorderListener(feature: DeviceFeature, fileManager: FileManager, devicePoolId: DevicePoolId,
                                        attachmentProviders: MutableList<AttachmentProvider>): NoOpTestRunListener =
            when (feature) {
                DeviceFeature.VIDEO -> {
                    ScreenRecorderTestRunListener(fileManager, devicePoolId, this)
                            .also { attachmentProviders.add(it) }
                }

                DeviceFeature.SCREENSHOT -> {
                    ScreenCapturerTestRunListener(fileManager, devicePoolId, this)
                            .also { attachmentProviders.add(it) }
                }
            }

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
