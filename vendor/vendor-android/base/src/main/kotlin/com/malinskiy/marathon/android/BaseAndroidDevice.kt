package com.malinskiy.marathon.android

import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.exception.InvalidSerialConfiguration
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.android.executor.listeners.LogCatListener
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.executor.listeners.ProgressTestRunListener
import com.malinskiy.marathon.android.executor.listeners.TestRunResultsListener
import com.malinskiy.marathon.android.executor.listeners.screenshot.ScreenCapturerTestRunListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderTestRunListener
import com.malinskiy.marathon.android.serial.SerialStrategy
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.exceptions.DeviceSetupException
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.system.measureTimeMillis

abstract class BaseAndroidDevice(
    protected val adbSerial: String,
    protected val serialStrategy: SerialStrategy,
    protected val track: Track,
    protected val timer: Timer
) : AndroidDevice, CoroutineScope {
    protected val logger = MarathonLogging.logger(AndroidDevice::class.java.simpleName)

    override var abi: String = "Unknown"
    override var version: AndroidVersion = AndroidVersion.DEFAULT
    override var externalStorageMount: String = "Unknown"
    override var model: String = "Unknown"
    override var manufacturer: String = "Unknown"
    override var deviceFeatures: Collection<DeviceFeature> = emptyList()
    override var apiLevel: Int = version.apiLevel
    override var operatingSystem: OperatingSystem = OperatingSystem(version.apiString)
    var realSerialNumber: String = "Unknown"
    val booted: Boolean
        get() = runBlocking {
            return@runBlocking withTimeoutOrNull(ADB_SHORT_TIMEOUT_MILLIS) {
                val bootedProperty: String? = getProperty("sys.boot_completed", true)
                bootedProperty != null
            } ?: false
        }
    override val serialNumber: String
        get() = when {
            booted -> realSerialNumber
            else -> adbSerial
        }

    override val fileManager = RemoteFileManager(this)
    protected lateinit var md5cmd: String

    override suspend fun setup() {
        waitForBoot()
        abi = getProperty("ro.product.cpu.abi") ?: abi

        val sdk = getProperty("ro.build.version.sdk")
        val codename = getProperty("ro.build.version.codename")
        version = if (sdk != null && codename != null) {
            AndroidVersion(sdk.toInt(), codename)
        } else AndroidVersion.DEFAULT
        apiLevel = version.apiLevel
        operatingSystem = OperatingSystem(version.apiString)
        model = getProperty("ro.product.model") ?: "Unknown"
        manufacturer = getProperty("ro.product.manufacturer") ?: "Unknown"

        externalStorageMount = safeExecuteShellCommand("echo \$EXTERNAL_STORAGE")?.trim {
            when (it) {
                '\n' -> true
                '\r' -> true
                else -> false
            }
        }
            ?: throw DeviceSetupException("Unable to configure device $serialNumber: externalStorageMount")

        deviceFeatures = detectFeatures()
        realSerialNumber = detectRealSerialNumber()
        md5cmd = detectMd5Binary()
    }

    /**
     * We can only do this after the device finished booting
     */
    private suspend fun detectRealSerialNumber(): String {
        val marathonSerialProp: String = getProperty("marathon.serialno") ?: ""
        val serialProp: String = getProperty("ro.boot.serialno") ?: ""
        val hostName: String = getProperty("net.hostname") ?: ""
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

        return result.apply {
            if (this == null) throw InvalidSerialConfiguration(serialStrategy)
        }
    }

    private suspend fun detectFeatures(): List<DeviceFeature> {
        val hasScreenRecord = when {
            !version.isGreaterOrEqualThan(19) -> false
            else -> hasBinary("/system/bin/screenrecord")
        }
        val videoSupport = hasScreenRecord && manufacturer != "Genymotion"
        val screenshotSupport = version.isGreaterOrEqualThan(AndroidVersion.VersionCodes.JELLY_BEAN)

        val features = mutableListOf<DeviceFeature>()

        if (videoSupport) features.add(DeviceFeature.VIDEO)
        if (screenshotSupport) features.add(DeviceFeature.SCREENSHOT)
        return features
    }


    override suspend fun safeClearPackage(packageName: String): String? =
        safeExecuteShellCommand("pm clear $packageName", "Could not clear package $packageName on device: $serialNumber")

    protected suspend fun clearLogcat() = safeExecuteShellCommand("logcat -c", "Could not clear logcat on device: $serialNumber")

    protected suspend fun waitForRemoteFileSync(
        md5: String,
        remotePath: String
    ) {
        if (md5.isNotEmpty() && md5cmd.isNotEmpty()) {
            val syncTimeMillis = measureTimeMillis {
                do {
                    val remoteMd5 = safeExecuteShellCommand("$md5cmd $remotePath") ?: ""
                    delay(10)
                } while (!remoteMd5.contains(md5))
            }
            logger.debug { "$remotePath synced in ${syncTimeMillis}ms" }
        } else {
            logger.warn { "no md5 was calculated for $remotePath. unable to sync" }
        }
    }

    private suspend fun hasBinary(path: String): Boolean {
        val output = safeExecuteShellCommand("ls $path")
        val value: String = output?.trim { it <= ' ' } ?: return false
        return !value.endsWith("No such file or directory")
    }

    private suspend fun waitForBoot(): Boolean {
        var booted = false

        track.trackProviderDevicePreparing(this) {
            for (i in 1..30) {
                if (getProperty("sys.boot_completed", false) != null) {
                    logger.debug { "Device $serialNumber booted!" }
                    booted = true
                    break
                } else {
                    delay(1000)
                    logger.debug { "Device $serialNumber is still booting..." }
                }

                if (Thread.interrupted() || !isActive) {
                    booted = true
                    break
                }
            }
        }

        return booted
    }

    protected fun createExecutionListeners(
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
                ScreenRecorderTestRunListener(fileManager, devicePoolId, this, screenRecordingPolicy, this)
                    .also { attachmentProviders.add(it) }
            }

            DeviceFeature.SCREENSHOT -> {
                ScreenCapturerTestRunListener(fileManager, devicePoolId, this, screenRecordingPolicy, this)
                    .also { attachmentProviders.add(it) }
            }
        }

    private fun selectRecorderType(preferred: DeviceFeature?, features: Collection<DeviceFeature>) = when {
        features.contains(preferred) -> preferred
        features.contains(DeviceFeature.VIDEO) -> DeviceFeature.VIDEO
        features.contains(DeviceFeature.SCREENSHOT) -> DeviceFeature.SCREENSHOT
        else -> null
    }

    private suspend fun detectMd5Binary(): String {
        for (path in listOf("/system/bin/md5", "/system/bin/md5sum")) {
            if (hasBinary(path)) return path.split("/").last()
        }
        return ""
    }

    override fun toString(): String {
        return "AndroidDevice(model=$model, serial=$serialNumber)"
    }
}
