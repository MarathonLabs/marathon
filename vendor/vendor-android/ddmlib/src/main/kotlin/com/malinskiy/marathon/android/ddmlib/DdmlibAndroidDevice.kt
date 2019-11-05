package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.android.ddmlib.IDevice.MNT_EXTERNAL_STORAGE
import com.android.ddmlib.InstallException
import com.android.ddmlib.NullOutputReceiver
import com.android.ddmlib.RawImage
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.SyncException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.logcat.LogCatMessage
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier
import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.ddmlib.shell.receiver.CollectingShellOutputReceiver
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.exception.InvalidSerialConfiguration
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.android.executor.listeners.LogCatListener
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.executor.listeners.ProgressTestRunListener
import com.malinskiy.marathon.android.executor.listeners.TestRunResultsListener
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.android.executor.listeners.screenshot.ScreenCapturerTestRunListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderOptions
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
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class DdmlibAndroidDevice(
    val ddmsDevice: IDevice,
    private val track: Track,
    private val timer: Timer,
    private val serialStrategy: SerialStrategy
) : Device, CoroutineScope, AndroidDevice {
    override val fileManager = RemoteFileManager(this)

    override val version: AndroidVersion = ddmsDevice.version
    override fun removeLogcatListener(listener: LineListener) {
        logcatListeners.getAndUpdate { it.apply { remove(listener) } }
    }

    private val nullOutputReceiver = NullOutputReceiver()

    private val receiver = LogCatReceiverTask(ddmsDevice)

    private var logcatThread = thread(name = "LogCatLogger-${ddmsDevice.serialNumber}") {
        receiver.run()
    }
    private val ref = AtomicReference<MutableList<LogCatMessage>>(mutableListOf())
    private val logcatListeners = AtomicReference<MutableList<LineListener>>(mutableListOf())
    private val listener: (MutableList<LogCatMessage>) -> Unit = {
        it.forEach { msg ->
            logcatListeners.get().forEach { listener ->
                listener.onLine("${msg.timestamp} ${msg.pid}-${msg.tid}/${msg.appName} ${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}")
            }
        }
    }

    override fun pullFile(remoteFilePath: String, localFilePath: String) {
        try {
            ddmsDevice.pullFile(remoteFilePath, localFilePath)
        } catch (e: SyncException) {
            throw TransferException(e)
        }
    }

    override fun getExternalStorageMount(): String = ddmsDevice.getMountPoint(MNT_EXTERNAL_STORAGE)!!

    override fun executeCommand(command: String, errorMessage: String) {
        try {
            ddmsDevice.safeExecuteShellCommand(command, nullOutputReceiver)
        } catch (e: TimeoutException) {
            logger.error(errorMessage, e)
        } catch (e: AdbCommandRejectedException) {
            logger.error(errorMessage, e)
        } catch (e: ShellCommandUnresponsiveException) {
            logger.error(errorMessage, e)
        } catch (e: IOException) {
            logger.error(errorMessage, e)
        }
    }

    override fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage {
        return try {
            val rawImage = ddmsDevice.getScreenshot(timeout, units)
            bufferedImageFrom(rawImage)
        } catch (e: TimeoutException) {
            throw java.util.concurrent.TimeoutException(e.message)
        } catch (e: AdbCommandRejectedException) {
            throw CommandRejectedException(e)
        }
    }

    override fun safeStartScreenRecorder(
        remoteFilePath: String,
        listener: LineListener,
        options: ScreenRecorderOptions
    ) {
        val recorderOptions = com.android.ddmlib.ScreenRecorderOptions.Builder()
            .setBitRate(options.bitrateMbps)
            .setShowTouches(options.showTouches)
            .setSize(options.width, options.height)
            .setTimeLimit(options.timeLimit, options.timeLimitUnits)
            .build()

        ddmsDevice.safeStartScreenRecorder(
            remoteFilePath,
            recorderOptions,
            CollectingOutputReceiver()
        )
    }

    override fun addLogcatListener(listener: LineListener) {
        receiver.addLogCatListener {
            it.forEach { message ->
                listener.onLine(message.toString())
            }
        }
    }

    private fun bufferedImageFrom(rawImage: RawImage): BufferedImage {
        val image = BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB)

        var index = 0
        val bytesPerPixel = rawImage.bpp shr 3
        for (y in 0 until rawImage.height) {
            for (x in 0 until rawImage.width) {
                image.setRGB(x, y, rawImage.getARGB(index) or -0x1000000)
                index += bytesPerPixel
            }
        }
        return image
    }

    private val dispatcher by lazy {
        newFixedThreadPoolContext(1, "AndroidDevice - execution - ${ddmsDevice.serialNumber}")
    }

    override val coroutineContext: CoroutineContext = dispatcher

    val logger = MarathonLogging.logger(DdmlibAndroidDevice::class.java.simpleName)

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
    override val apiLevel: Int
        get() = ddmsDevice.version.apiLevel

    override fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        return try {
            ddmsDevice.safeInstallPackage(absolutePath, reinstall, optionalParams)
        } catch (e: InstallException) {
            throw com.malinskiy.marathon.android.exception.InstallException(e)
        }
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

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {

        val deferredResult = async {
            val listener = createListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
                .toDdmlibTestListener()
            AndroidDeviceTestRunner(this@DdmlibAndroidDevice)
                .execute(configuration, testBatch, listener)
        }
        deferredResult.await()
    }

    private fun createListeners(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ): CompositeTestRunListener {
        val fileManager = FileManager(configuration.outputDir)
        val attachmentProviders = mutableListOf<AttachmentProvider>()

        val features = this.deviceFeatures

        val preferableRecorderType = configuration.vendorConfiguration.preferableRecorderType()
        val recorderListener = selectRecorderType(preferableRecorderType, features)?.let { feature ->
            prepareRecorderListener(feature, fileManager, devicePoolId, attachmentProviders)
        } ?: NoOpTestRunListener()

        val logCatListener = LogCatListener(this, devicePoolId, LogWriter(fileManager))
            .also { attachmentProviders.add(it) }

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
                AndroidAppInstaller(configuration).prepareInstallation(this@DdmlibAndroidDevice)
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

    override fun safeUninstallPackage(appPackage: String): String? {
        return try {
            ddmsDevice.safeUninstallPackage(appPackage)
        } catch (e: InstallException) {
            throw com.malinskiy.marathon.android.exception.InstallException(e)
        }
    }

    override fun safeExecuteShellCommand(command: String): String {
        val receiver = CollectingShellOutputReceiver()
        ddmsDevice.safeExecuteShellCommand(command, receiver)
        return receiver.output()
    }

    private fun prepareRecorderListener(
        feature: DeviceFeature, fileManager: FileManager, devicePoolId: DevicePoolId,
        attachmentProviders: MutableList<AttachmentProvider>
    ): NoOpTestRunListener =
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

private fun AndroidTestRunListener.toDdmlibTestListener(): ITestRunListener {
    return object : ITestRunListener {
        override fun testRunStarted(runName: String?, testCount: Int) {
            this@toDdmlibTestListener.testRunStarted(runName ?: "", testCount)
        }

        override fun testStarted(test: TestIdentifier) {
            this@toDdmlibTestListener.testStarted(test.toMarathonTestIdentifier())
        }

        override fun testAssumptionFailure(test: TestIdentifier, trace: String?) {
            this@toDdmlibTestListener.testAssumptionFailure(test.toMarathonTestIdentifier(), trace ?: "")
        }

        override fun testRunStopped(elapsedTime: Long) {
            this@toDdmlibTestListener.testRunStopped(elapsedTime)
        }

        override fun testFailed(test: TestIdentifier, trace: String?) {
            this@toDdmlibTestListener.testFailed(test.toMarathonTestIdentifier(), trace ?: "")
        }

        override fun testEnded(test: TestIdentifier, testMetrics: MutableMap<String, String>?) {
            this@toDdmlibTestListener.testEnded(test.toMarathonTestIdentifier(), testMetrics ?: emptyMap())
        }

        override fun testIgnored(test: TestIdentifier) {
            this@toDdmlibTestListener.testIgnored(test.toMarathonTestIdentifier())
        }

        override fun testRunFailed(errorMessage: String?) {
            this@toDdmlibTestListener.testRunFailed(errorMessage ?: "")
        }

        override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
            this@toDdmlibTestListener.testRunEnded(elapsedTime, runMetrics ?: emptyMap())
        }

    }
}

private fun TestIdentifier.toMarathonTestIdentifier() = com.malinskiy.marathon.android.model.TestIdentifier(this.className, this.testName)
