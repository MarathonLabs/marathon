package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.android.ddmlib.InstallException
import com.android.ddmlib.RawImage
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.SyncException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.logcat.LogCatMessage
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.BaseAndroidDevice
import com.malinskiy.marathon.android.ddmlib.shell.receiver.CollectingShellOutputReceiver
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
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
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class DdmlibAndroidDevice(
    val ddmsDevice: IDevice,
    adbSerial: String,
    track: Track,
    timer: Timer,
    serialStrategy: SerialStrategy
) : BaseAndroidDevice(adbSerial, serialStrategy, track, timer) {

    override suspend fun setup() {
        super.setup()
    }

    override suspend fun getProperty(name: String, cached: Boolean): String? = ddmsDevice.getProperty(name)

    private val receiver = LogCatReceiverTask(ddmsDevice)

    private var logcatThread = thread(name = "LogCatLogger-${ddmsDevice.serialNumber}") {
        receiver.run()
    }

    private val logcatListeners = mutableListOf<LineListener>()
    private val listener: (List<LogCatMessage>) -> Unit = {
        it.forEach { msg ->
            synchronized(logcatListeners) {
                logcatListeners.forEach { listener ->
                    listener.onLine("${msg.timestamp} ${msg.pid}-${msg.tid}/${msg.appName} ${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}")
                }
            }
        }
    }

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

    override suspend fun pullFile(remoteFilePath: String, localFilePath: String) {
        try {
            ddmsDevice.pullFile(remoteFilePath, localFilePath)
        } catch (e: SyncException) {
            throw TransferException(e)
        }
    }

    override suspend fun pushFile(localFilePath: String, remoteFilePath: String) {
        try {
            ddmsDevice.pushFile(localFilePath, remoteFilePath)
        } catch (e: SyncException) {
            throw TransferException(e)
        }
    }

    override suspend fun executeShellCommand(command: String, errorMessage: String): String? {
        try {
            val outputReceiver = SimpleOutputReceiver()
            ddmsDevice.safeExecuteShellCommand(command, outputReceiver)
            return outputReceiver.output()
        } catch (e: TimeoutException) {
            logger.error(errorMessage, e)
            return null
        } catch (e: AdbCommandRejectedException) {
            logger.error(errorMessage, e)
            return null
        } catch (e: ShellCommandUnresponsiveException) {
            logger.error(errorMessage, e)
            return null
        } catch (e: IOException) {
            logger.error(errorMessage, e)
            return null
        }
    }

    override suspend fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage {
        return try {
            val rawImage = ddmsDevice.getScreenshot(timeout, units)
            bufferedImageFrom(rawImage)
        } catch (e: TimeoutException) {
            throw java.util.concurrent.TimeoutException(e.message)
        } catch (e: AdbCommandRejectedException) {
            throw CommandRejectedException(e)
        }
    }

    override suspend fun safeStartScreenRecorder(
        remoteFilePath: String,
        options: ScreenRecorderOptions
    ) {
        val outputReceiver = CollectingOutputReceiver()
        ddmsDevice.safeStartScreenRecorder(
            remoteFilePath,
            options,
            outputReceiver
        )
        logger.debug { "screenrecord output:\n ${outputReceiver.output}" }
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
        newFixedThreadPoolContext(2, "AndroidDevice - execution - ${ddmsDevice.serialNumber}")
    }

    override val coroutineContext: CoroutineContext = dispatcher

    override suspend fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        return try {
            ddmsDevice.safeInstallPackage(absolutePath, reinstall, optionalParams)
        } catch (e: InstallException) {
            throw com.malinskiy.marathon.android.exception.InstallException(e)
        }
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
            val listener = createExecutionListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
                .toDdmlibTestListener()
            AndroidDeviceTestRunner(this@DdmlibAndroidDevice)
                .execute(configuration, testBatch, listener)
        }
        deferredResult.await()
    }

    override suspend fun prepare(configuration: Configuration) {
        track.trackDevicePreparing(this) {
            val deferred = async {
                AndroidAppInstaller(configuration).prepareInstallation(this@DdmlibAndroidDevice)
                fileManager.removeRemoteDirectory()
                fileManager.createRemoteDirectory()
                clearLogcat()
                receiver.addLogCatListener(listener)

            }
            deferred.await()
        }
    }

    override fun dispose() {
        logcatThread.interrupt()
        receiver.removeLogCatListener(listener)
        dispatcher.close()
    }

    override suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean): String? {
        return try {
            ddmsDevice.safeUninstallPackage(appPackage, keepData)
        } catch (e: InstallException) {
            throw com.malinskiy.marathon.android.exception.InstallException(e)
        }
    }

    override suspend fun safeClearPackage(packageName: String): String? = ddmsDevice.safeClearPackage(packageName)

    override suspend fun safeExecuteShellCommand(command: String, errorMessage: String): String? {
        val receiver = CollectingShellOutputReceiver()
        ddmsDevice.safeExecuteShellCommand(command, receiver)
        return receiver.output()
    }
}

private fun AndroidTestRunListener.toDdmlibTestListener(): ITestRunListener {
    return object : ITestRunListener {
        override fun testRunStarted(runName: String?, testCount: Int) {
            runBlocking {
                this@toDdmlibTestListener.testRunStarted(runName ?: "", testCount)
            }
        }

        override fun testStarted(test: TestIdentifier) {
            runBlocking {
                this@toDdmlibTestListener.testStarted(test.toMarathonTestIdentifier())
            }
        }

        override fun testAssumptionFailure(test: TestIdentifier, trace: String?) {
            runBlocking {
                this@toDdmlibTestListener.testAssumptionFailure(test.toMarathonTestIdentifier(), trace ?: "")
            }
        }

        override fun testRunStopped(elapsedTime: Long) {
            runBlocking {
                this@toDdmlibTestListener.testRunStopped(elapsedTime)
            }
        }

        override fun testFailed(test: TestIdentifier, trace: String?) {
            runBlocking {
                this@toDdmlibTestListener.testFailed(test.toMarathonTestIdentifier(), trace ?: "")
            }
        }

        override fun testEnded(test: TestIdentifier, testMetrics: MutableMap<String, String>?) {
            runBlocking {
                this@toDdmlibTestListener.testEnded(test.toMarathonTestIdentifier(), testMetrics ?: emptyMap())
            }
        }

        override fun testIgnored(test: TestIdentifier) {
            runBlocking {
                this@toDdmlibTestListener.testIgnored(test.toMarathonTestIdentifier())
            }
        }

        override fun testRunFailed(errorMessage: String?) {
            runBlocking {
                this@toDdmlibTestListener.testRunFailed(errorMessage ?: "")
            }
        }

        override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
            runBlocking {
                this@toDdmlibTestListener.testRunEnded(elapsedTime, runMetrics ?: emptyMap())
            }
        }

    }
}

private fun TestIdentifier.toMarathonTestIdentifier() = com.malinskiy.marathon.android.model.TestIdentifier(this.className, this.testName)
