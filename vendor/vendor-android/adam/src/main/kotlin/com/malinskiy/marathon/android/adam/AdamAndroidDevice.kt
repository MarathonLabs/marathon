package com.malinskiy.marathon.android.adam

import com.android.sdklib.AndroidVersion
import com.malinskiy.adam.AndroidDebugBridgeServer
import com.malinskiy.adam.request.devices.DeviceState
import com.malinskiy.adam.request.sync.GetPropRequest
import com.malinskiy.adam.request.sync.InstallRemotePackageRequest
import com.malinskiy.adam.request.sync.PullFileRequest
import com.malinskiy.adam.request.sync.PushFileRequest
import com.malinskiy.adam.request.sync.ShellCommandRequest
import com.malinskiy.adam.request.sync.UninstallRemotePackageRequest
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.exception.InvalidSerialConfiguration
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
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class AdamAndroidDevice(
    internal val server: AndroidDebugBridgeServer,
    private val deviceStateTracker: DeviceStateTracker,
    internal val adbSerial: String,
    private val track: Track,
    private val timer: Timer,
    private val serialStrategy: SerialStrategy
) : AndroidDevice, CoroutineScope {
    val logger = MarathonLogging.logger(AdamAndroidDevice::class.java.simpleName)

    private val dispatcher by lazy {
        newFixedThreadPoolContext(1, "AndroidDevice - execution - $adbSerial")
    }

    override val coroutineContext: CoroutineContext = dispatcher

    override fun getExternalStorageMount(): String {
        return runBlocking {
            return@runBlocking server.execute(ShellCommandRequest("echo \$EXTERNAL_STORAGE"), serial = adbSerial)
        }
    }

    override fun executeCommand(command: String, errorMessage: String) {
        runBlocking {
            try {
                server.execute(ShellCommandRequest(command), serial = adbSerial)
            } catch (e: Exception) {
                logger.error(errorMessage, e)
            }
        }
    }

    override fun pullFile(remoteFilePath: String, localFilePath: String) {
        val channel = server.execute(request = PullFileRequest(remoteFilePath, File(localFilePath)), serial = adbSerial, scope = this)

        runBlocking {
            var progress = 0.0
            while (!channel.isClosedForReceive) {
                progress = channel.receive()
            }

            if (progress != 1.0) {
                throw RuntimeException("Couldn't pull file $remoteFilePath from device $serialNumber")
            }
        }
    }

    override fun safeUninstallPackage(appPackage: String): String? {
        return runBlocking {
            return@runBlocking server.execute(UninstallRemotePackageRequest(appPackage, keepData = false), serial = adbSerial)
        }
    }

    override suspend fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        val file = File(absolutePath)
        val remotePath = "/data/local/tmp/${file.name}"
        val channel = server.execute(PushFileRequest(file, remotePath), serial = adbSerial, scope = this)
        var progress = 0.0
        while (!channel.isClosedForReceive && progress < 1.0) {
            progress = channel.receive()
        }
        if (progress != 1.0) {
            throw RuntimeException("Couldn't push file $absolutePath to device $serialNumber")
        }

        return server.execute(
            InstallRemotePackageRequest(
                remotePath,
                reinstall = reinstall,
                extraArgs = optionalParams.split(" ").toList() + " "
            ), serial = adbSerial
        )
    }

    override fun safeExecuteShellCommand(command: String): String {
        return runBlocking {
            try {
                return@runBlocking server.execute(ShellCommandRequest(command), serial = adbSerial)
            } catch (e: Exception) {
                //Ignore
                return@runBlocking ""
            }
        }
    }

    fun safeClearPackage(packageName: String): String? {
        return runBlocking {
            return@runBlocking server.execute(ShellCommandRequest("pm clear $packageName"), serial = adbSerial)
        }
    }

    override fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage {
        return BufferedImage(0, 0, 0)
    }

    override fun addLogcatListener(listener: LineListener) {
    }

    override fun removeLogcatListener(listener: LineListener) {
    }

    override fun safeStartScreenRecorder(remoteFilePath: String, listener: LineListener, options: ScreenRecorderOptions) {
    }

    private val props: Map<String, String>
        get() = runBlocking {
            server.execute(GetPropRequest(), serial = adbSerial)
        }

    override val version: AndroidVersion
        get() {
            val sdk = props["ro.build.version.sdk"]
            val codename = props["ro.build.version.codename"]
            return sdk?.let {
                AndroidVersion(sdk.toInt(), codename)
            } ?: AndroidVersion.DEFAULT
        }
    override val fileManager = RemoteFileManager(this)

    override val apiLevel = version.apiLevel

    /**
     * We can only call this after the device finished booting
     */
    private val realSerialNumber: String by lazy {
        val marathonSerialProp: String = props["marathon.serialno"] ?: ""
        val serialProp: String = props["ro.boot.serialno"] ?: ""
        val hostName: String = props["net.hostname"] ?: ""
        val serialNumber = adbSerial

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
        get() = runBlocking {
            val props: Map<String, String> = server.execute(GetPropRequest(), serial = adbSerial)
            val bootedProperty: String? = props["sys.boot_completed"]
            return@runBlocking bootedProperty != null
        }

    override val operatingSystem: OperatingSystem by lazy { OperatingSystem(version.apiString) }
    override val serialNumber: String = when {
        booted -> realSerialNumber
        else -> adbSerial
    }
    override val model
        get() = props["ro.product.model"] ?: "Unknown"
    override val manufacturer: String
        get() = props["ro.product.manufacturer"] ?: "Unknown"
    override val networkState: NetworkState = if (healthy) NetworkState.CONNECTED else NetworkState.DISCONNECTED
    override val deviceFeatures: Collection<DeviceFeature>
        get() {
            val hasScreenRecord = when {
                !version.isGreaterOrEqualThan(19) -> false
                else -> hasBinary("/system/bin/screenrecord")
            }
            val videoSupport = hasScreenRecord && manufacturer != "Genymotion"
            val screenshotSupport = version.isGreaterOrEqualThan(AndroidVersion.VersionCodes.JELLY_BEAN)

            val features = mutableListOf<DeviceFeature>()

            if (videoSupport) features.add(DeviceFeature.VIDEO)
            if (screenshotSupport) features.add(DeviceFeature.SCREENSHOT)

            //TODO: remove short circuit
            return emptyList()
//            return features
        }
    override val healthy: Boolean
        get() = when (deviceStateTracker.getState(adbSerial)) {
            DeviceState.DEVICE -> true
            else -> false
        }
    override val abi
        get() = props["ro.product.cpu.abi"] ?: "Unknown"

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        val listener = createListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
        AndroidDeviceTestRunner(this@AdamAndroidDevice).execute(configuration, testBatch, listener)
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
        val screenRecordingPolicy = configuration.screenRecordingPolicy
        val recorderListener = selectRecorderType(preferableRecorderType, features)?.let { feature ->
            prepareRecorderListener(feature, fileManager, devicePoolId, screenRecordingPolicy, attachmentProviders)
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

    private fun prepareRecorderListener(
        feature: DeviceFeature, fileManager: FileManager, devicePoolId: DevicePoolId, screenRecordingPolicy: ScreenRecordingPolicy,
        attachmentProviders: MutableList<AttachmentProvider>
    ): NoOpTestRunListener =
        when (feature) {
            DeviceFeature.VIDEO -> {
                ScreenRecorderTestRunListener(fileManager, devicePoolId, this, screenRecordingPolicy)
                    .also { attachmentProviders.add(it) }
            }

            DeviceFeature.SCREENSHOT -> {
                ScreenCapturerTestRunListener(fileManager, devicePoolId, this, screenRecordingPolicy)
                    .also { attachmentProviders.add(it) }
            }
        }

    private fun selectRecorderType(preferred: DeviceFeature?, features: Collection<DeviceFeature>) = when {
        features.contains(preferred) -> preferred
        features.contains(DeviceFeature.VIDEO) -> DeviceFeature.VIDEO
        features.contains(DeviceFeature.SCREENSHOT) -> DeviceFeature.SCREENSHOT
        else -> null
    }

    override suspend fun prepare(configuration: Configuration) {
        track.trackDevicePreparing(this) {
            AndroidAppInstaller(configuration).prepareInstallation(this@AdamAndroidDevice)
            fileManager.removeRemoteDirectory()
            fileManager.createRemoteDirectory()
            clearLogcat()
            //TODO
            //addLogCatListener(listener)
        }
    }

    private fun clearLogcat() = safeExecuteShellCommand("logcat -c")

    override fun dispose() {
        dispatcher.close()
    }

    private fun hasBinary(path: String) = runBlocking {
        val output = server.execute(ShellCommandRequest("ls $path"), serial = adbSerial)
        val value: String = output.trim { it <= ' ' }
        return@runBlocking !value.endsWith("No such file or directory")
    }
}
