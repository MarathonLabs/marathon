package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeServer
import com.malinskiy.adam.exception.PullFailedException
import com.malinskiy.adam.request.async.ChanneledLogcatRequest
import com.malinskiy.adam.request.async.LogcatReadMode
import com.malinskiy.adam.request.devices.DeviceState
import com.malinskiy.adam.request.sync.GetPropRequest
import com.malinskiy.adam.request.sync.InstallRemotePackageRequest
import com.malinskiy.adam.request.sync.PullFileRequest
import com.malinskiy.adam.request.sync.PushFileRequest
import com.malinskiy.adam.request.sync.ScreenCaptureRequest
import com.malinskiy.adam.request.sync.ShellCommandRequest
import com.malinskiy.adam.request.sync.UninstallRemotePackageRequest
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ADB_SCREEN_RECORD_TIMEOUT_MILLIS
import com.malinskiy.marathon.android.ADB_SHORT_TIMEOUT_MILLIS
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.BaseAndroidDevice
import com.malinskiy.marathon.android.adam.log.LogCatMessageParser
import com.malinskiy.marathon.android.adam.screenshot.ImageAdapter
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderOptions
import com.malinskiy.marathon.android.serial.SerialStrategy
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class AdamAndroidDevice(
    private val server: AndroidDebugBridgeServer,
    private val deviceStateTracker: DeviceStateTracker,
    adbSerial: String,
    track: Track,
    timer: Timer,
    serialStrategy: SerialStrategy
) : BaseAndroidDevice(adbSerial, serialStrategy, track, timer) {

    override suspend fun setup() {
        super.setup()

        fetchProps()
        logcatChannel = server.execute(
            ChanneledLogcatRequest(
                modes = listOf(LogcatReadMode.long)
            ), serial = adbSerial, scope = this
        )
        async(context = coroutineContext) {
            val parser = LogCatMessageParser()

            while (!logcatChannel.isClosedForReceive) {
                val logPart = logcatChannel.receive()
                val messages = parser.processLogLines(logPart.lines(), this@AdamAndroidDevice)
                synchronized(logcatListeners) {
                    messages.forEach { msg ->
                        logcatListeners.forEach { listener ->
                            listener.onLine("${msg.timestamp} ${msg.pid}-${msg.tid}/${msg.appName} ${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}")
                        }
                    }
                }
            }
        }
    }

    private val dispatcher by lazy {
        newFixedThreadPoolContext(2, "AndroidDevice - execution - $adbSerial")
    }
    private val imageAdapter = ImageAdapter()

    private lateinit var logcatChannel: ReceiveChannel<String>

    override val coroutineContext: CoroutineContext = dispatcher

    private var props: Map<String, String> = emptyMap()

    override suspend fun executeShellCommand(command: String, errorMessage: String): String? {
        return try {
            return server.execute(ShellCommandRequest(command), serial = adbSerial)
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    override suspend fun safeExecuteShellCommand(command: String, errorMessage: String): String? {
        return try {
            return withTimeoutOrNull(ADB_SHORT_TIMEOUT_MILLIS) {
                return@withTimeoutOrNull server.execute(ShellCommandRequest(command), serial = adbSerial)
            }
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    override suspend fun pullFile(remoteFilePath: String, localFilePath: String) {
        var progress: Double = 0.0
        try {
            val channel = server.execute(request = PullFileRequest(remoteFilePath, File(localFilePath)), serial = adbSerial, scope = this)
            while (!channel.isClosedForReceive) {
                progress = channel.receiveOrNull() ?: break
            }
        } catch (e: PullFailedException) {
            throw TransferException("Couldn't pull file $remoteFilePath from device $serialNumber")
        }

        if (progress != 1.0) {
            throw TransferException("Couldn't pull file $remoteFilePath from device $serialNumber")
        }
    }

    override suspend fun pushFile(localFilePath: String, remoteFilePath: String) {
        val file = File(localFilePath)
        val channel = server.execute(PushFileRequest(file, remoteFilePath), serial = adbSerial, scope = this)
        var progress = 0.0
        while (!channel.isClosedForReceive && progress < 1.0) {
            progress = channel.receiveOrNull() ?: break
        }
        if (progress != 1.0) {
            throw TransferException("Couldn't push file $localFilePath to device $serialNumber:$remoteFilePath")
        }
    }

    override suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean): String? {
        return server.execute(UninstallRemotePackageRequest(appPackage, keepData = keepData), serial = adbSerial)
    }

    override suspend fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        val file = File(absolutePath)
        val remotePath = "/data/local/tmp/${file.name}"

        pushFile(absolutePath, remotePath)

        return server.execute(
            InstallRemotePackageRequest(
                remotePath,
                reinstall = reinstall,
                extraArgs = optionalParams.split(" ").toList() + " "
            ), serial = adbSerial
        )
    }

    override suspend fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage {
        val rawImage = server.execute(ScreenCaptureRequest(), serial = adbSerial)
        return imageAdapter.convert(rawImage)
    }

    private val logcatListeners = mutableListOf<LineListener>()

    override fun addLogcatListener(listener: LineListener) {
        synchronized(logcatListeners) {
            logcatListeners.add(listener)
        }
    }

    override fun removeLogcatListener(listener: LineListener) {
        synchronized(logcatListeners) {
            logcatListeners.remove(listener)
        }
    }

    override suspend fun safeStartScreenRecorder(
        remoteFilePath: String,
        options: ScreenRecorderOptions
    ) {
        val screenRecorderCommand = options.toScreenRecorderCommand(remoteFilePath)
        try {
            withTimeoutOrNull(ADB_SCREEN_RECORD_TIMEOUT_MILLIS) {
                val output = server.execute(ShellCommandRequest(screenRecorderCommand), serial = adbSerial)
                logger.debug { "screenrecord output:\n $output" }
            }
        } catch (e: Exception) {
            logger.error("Unable to start screenrecord", e)
        }
    }

    override suspend fun getProperty(name: String, cached: Boolean): String? = when (cached) {
        true -> props[name]
        false -> {
            fetchProps()
            props[name]
        }
    }

    private suspend fun fetchProps() {
        val map = server.execute(GetPropRequest(), serial = adbSerial)
        props = map
    }

    override val networkState: NetworkState = if (healthy) NetworkState.CONNECTED else NetworkState.DISCONNECTED

    override val healthy: Boolean
        get() = when (deviceStateTracker.getState(adbSerial)) {
            DeviceState.DEVICE -> true
            else -> false
        }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        val deferredResult = async(context = coroutineContext) {
            val listener = createExecutionListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
            AndroidDeviceTestRunner(this@AdamAndroidDevice).execute(configuration, testBatch, listener)
        }

        deferredResult.await()
    }

    override suspend fun prepare(configuration: Configuration) {
        track.trackDevicePreparing(this) {
            AndroidAppInstaller(configuration).prepareInstallation(this@AdamAndroidDevice)
            fileManager.removeRemoteDirectory()
            fileManager.createRemoteDirectory()
            clearLogcat()
        }
    }

    override fun dispose() {
        dispatcher.close()
    }

    fun executeTestRequest(runnerRequest: TestRunnerRequest): ReceiveChannel<String> {
        return server.execute(runnerRequest, scope = this, serial = adbSerial)
    }
}
