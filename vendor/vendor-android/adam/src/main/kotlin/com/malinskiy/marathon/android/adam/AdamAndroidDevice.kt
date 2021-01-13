package com.malinskiy.marathon.android.adam

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.exception.PullFailedException
import com.malinskiy.adam.exception.PushFailedException
import com.malinskiy.adam.exception.RequestRejectedException
import com.malinskiy.adam.exception.UnsupportedImageProtocolException
import com.malinskiy.adam.exception.UnsupportedSyncProtocolException
import com.malinskiy.adam.request.device.DeviceState
import com.malinskiy.adam.request.forwarding.LocalTcpPortSpec
import com.malinskiy.adam.request.forwarding.RemoteTcpPortSpec
import com.malinskiy.adam.request.framebuffer.BufferedImageScreenCaptureAdapter
import com.malinskiy.adam.request.framebuffer.ScreenCaptureRequest
import com.malinskiy.adam.request.logcat.ChanneledLogcatRequest
import com.malinskiy.adam.request.logcat.LogcatReadMode
import com.malinskiy.adam.request.pkg.InstallRemotePackageRequest
import com.malinskiy.adam.request.pkg.UninstallRemotePackageRequest
import com.malinskiy.adam.request.prop.GetPropRequest
import com.malinskiy.adam.request.reverse.RemoveReversePortForwardRequest
import com.malinskiy.adam.request.reverse.ReversePortForwardRequest
import com.malinskiy.adam.request.reverse.ReversePortForwardingRule
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import com.malinskiy.adam.request.sync.AndroidFile
import com.malinskiy.adam.request.sync.AndroidFileType
import com.malinskiy.adam.request.sync.ListFilesRequest
import com.malinskiy.adam.request.sync.v1.PullFileRequest
import com.malinskiy.adam.request.sync.v1.PushFileRequest
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.BaseAndroidDevice
import com.malinskiy.marathon.android.VideoConfiguration
import com.malinskiy.marathon.android.adam.log.LogCatMessageParser
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.exception.InstallException
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.extension.withTimeout
import com.malinskiy.marathon.extension.withTimeoutOrNull
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class AdamAndroidDevice(
    private val client: AndroidDebugBridgeClient,
    private val deviceStateTracker: DeviceStateTracker,
    adbSerial: String,
    configuration: AndroidConfiguration,
    track: Track,
    timer: Timer,
    serialStrategy: SerialStrategy
) : BaseAndroidDevice(adbSerial, serialStrategy, configuration, track, timer) {

    /**
     * This adapter is thread-safe but the internal reusable buffer should be considered if we ever need to make screenshots in parallel
     */
    private val imageScreenCaptureAdapter = BufferedImageScreenCaptureAdapter()

    val portForwardingRules = mutableMapOf<String, ReversePortForwardingRule>()

    override suspend fun setup() {
        super.setup()

        fetchProps()
        logcatChannel = client.execute(
            ChanneledLogcatRequest(
                modes = listOf(LogcatReadMode.long)
            ), serial = adbSerial, scope = this
        )
        async {
            val parser = LogCatMessageParser()

            while (!logcatChannel.isClosedForReceive) {
                val logPart = logcatChannel.receiveOrNull() ?: continue
                val messages = parser.processLogLines(logPart.lines(), this@AdamAndroidDevice)
                //TODO: replace with Mutex.lock after the removal of ddmlib
                synchronized(logcatListeners) {
                    messages.forEach { msg ->
                        logcatListeners.forEach { listener ->
                            listener.onLine("${msg.timestamp} ${msg.pid}-${msg.tid}/${msg.appName} ${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}")
                        }
                    }
                }
            }
        }

        setupReversePortForwarding()
    }

    private val dispatcher by lazy {
        newFixedThreadPoolContext(1, "AndroidDevice - execution - $adbSerial")
    }

    private lateinit var logcatChannel: ReceiveChannel<String>

    override val coroutineContext: CoroutineContext = dispatcher

    private var props: Map<String, String> = emptyMap()

    override suspend fun executeShellCommand(command: String, errorMessage: String): String? {
        return try {
            return client.execute(ShellCommandRequest(command), serial = adbSerial).output
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    override suspend fun safeExecuteShellCommand(command: String, errorMessage: String): String? {
        return try {
            withTimeoutOrNull(configuration.timeoutConfiguration.shell) {
                client.execute(ShellCommandRequest(command), serial = adbSerial).output
            }
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    override suspend fun criticalExecuteShellCommand(command: String, errorMessage: String): String {
        return withTimeoutOrNull(configuration.timeoutConfiguration.shell) {
            client.execute(ShellCommandRequest(command), serial = adbSerial).output
        } ?: throw CommandRejectedException(errorMessage)
    }

    override suspend fun pullFile(remoteFilePath: String, localFilePath: String) {
        var progress: Double = 0.0
        try {
            val channel = client.execute(request = PullFileRequest(remoteFilePath, File(localFilePath)), serial = adbSerial, scope = this)
            while (!channel.isClosedForReceive) {
                progress = channel.receiveOrNull() ?: break
            }
        } catch (e: PullFailedException) {
            throw TransferException("Couldn't pull file $remoteFilePath from device $serialNumber")
        } catch (e: UnsupportedSyncProtocolException) {
            throw TransferException("Device $serialNumber does not support sync: file transfer")
        }

        if (progress != 1.0) {
            throw TransferException("Couldn't pull file $remoteFilePath from device $serialNumber")
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun pushFile(localFilePath: String, remoteFilePath: String, verify: Boolean) {
        val file = File(localFilePath)
        var progress: Double = 0.0

        try {
            val channel = client.execute(PushFileRequest(file, remoteFilePath), serial = adbSerial, scope = this)
            while (!channel.isClosedForReceive) {
                progress = channel.receiveOrNull() ?: break
            }
        } catch (e: PushFailedException) {
            throw TransferException(e)
        }

        if (progress != 1.0) {
            throw TransferException("Couldn't push file $localFilePath to device $serialNumber:$remoteFilePath. Last progress: $progress")
        }

        if (verify) {
            val expectedMd5 = Files.asByteSource(File(localFilePath)).hash(Hashing.md5()).toString()
            waitForRemoteFileSync(expectedMd5, remoteFilePath)
        }
    }

    override suspend fun pullFolder(remoteFolderPath: String, localFolderPath: String) {
        if (!File(localFolderPath).isDirectory) {
            throw TransferException("Destination $localFolderPath is not a directory")
        }

        /**
         * Iterate instead of recursion
         */
        val filesToPull = mutableListOf<AndroidFile>()
        var directoriesToTraverse = listOf(remoteFolderPath)

        while (directoriesToTraverse.isNotEmpty()) {
            //We have to use a second collection because we're iterating over directoriesToTraverse later
            val currentDepthDirs = mutableListOf<String>()
            for (dir in directoriesToTraverse) {
                withTimeoutOrNull(configuration.timeoutConfiguration.listFiles) {
                    val currentDepthFiles = client.execute(request = ListFilesRequest(dir), serial = adbSerial)

                    filesToPull.addAll(currentDepthFiles.filter { it.type == AndroidFileType.REGULAR_FILE })
                    currentDepthDirs.addAll(
                        currentDepthFiles.filter { it.type == AndroidFileType.DIRECTORY }
                            .map { it.directory + '/' + it.name }
                    )
                } ?: logger.warn { "Listing $dir timed out. Ignoring the rest" }
            }
            directoriesToTraverse = currentDepthDirs
        }

        filesToPull.forEach { file ->
            val relativePathSegments = file.directory.substringAfter(remoteFolderPath).split('\\')
            val absoluteLocalDirectory = StringBuilder().apply {
                append(localFolderPath)
                append(File.separator)
                relativePathSegments.forEach { segment ->
                    append(segment)
                    append(File.separator)
                }
            }.toString()

            val localFileDirectory = File(absoluteLocalDirectory).apply {
                mkdirs()
            }
            val localFile = File(localFileDirectory, file.name)
            val remoteFilePath = "${file.directory}${File.separator}${file.name}"

            withTimeoutOrNull(configuration.timeoutConfiguration.pullFile) {
                pullFile(remoteFilePath, localFile.absolutePath)
            } ?: logger.warn { "Pulling $remoteFilePath timed out. Ignoring" }
        }
    }

    override suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean): String? {
        return withTimeoutOrNull(configuration.timeoutConfiguration.uninstall) {
            client.execute(UninstallRemotePackageRequest(appPackage, keepData = keepData), serial = adbSerial).output
        }
    }

    override suspend fun installPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        val file = File(absolutePath)
        val remotePath = "/data/local/tmp/${file.name}"

        try {
            withTimeoutOrNull(configuration.timeoutConfiguration.pushFile) {
                pushFile(absolutePath, remotePath, verify = true)
            } ?: throw InstallException("Timeout transferring $absolutePath")
        } catch (e: TransferException) {
            throw InstallException(e)
        }

        val result = withTimeoutOrNull(configuration.timeoutConfiguration.install) {
            client.execute(
                InstallRemotePackageRequest(
                    remotePath,
                    reinstall = reinstall,
                    extraArgs = optionalParams.split(" ").toList() + " "
                ), serial = adbSerial
            )
        } ?: throw InstallException("Timeout transferring $absolutePath")

        safeExecuteShellCommand("rm $remotePath")
        return result.output.trim()
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        return try {
            withTimeoutOrNull(timeout) {
                client.execute(ScreenCaptureRequest(imageScreenCaptureAdapter), serial = adbSerial)
            }
        } catch (e: UnsupportedImageProtocolException) {
            logger.warn(e) { "Unable to retrieve screenshot from device $adbSerial" }
            null
        }
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
        options: VideoConfiguration
    ) {
        val screenRecorderCommand = options.toScreenRecorderCommand(remoteFilePath)
        try {
            withTimeoutOrNull(configuration.timeoutConfiguration.screenrecorder) {
                val output = client.execute(ShellCommandRequest(screenRecorderCommand), serial = adbSerial)
                logger.debug { "screenrecord output:\n $output" }
            }
        } catch (e: CancellationException) {
            //Ignore
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
        val map = client.execute(GetPropRequest(), serial = adbSerial)
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
        try {
            supervisorScope {
                val listener = createExecutionListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
                AndroidDeviceTestRunner(this@AdamAndroidDevice).execute(configuration, testBatch, listener)
            }
        } catch (e: RequestRejectedException) {
            throw DeviceLostException(e)
        } catch (e: CommandRejectedException) {
            throw DeviceLostException(e)
        }
    }

    override suspend fun prepare(configuration: Configuration) = supervisorScope {
        track.trackDevicePreparing(this@AdamAndroidDevice) {
            AndroidAppInstaller(configuration).prepareInstallation(this@AdamAndroidDevice)
            fileManager.removeRemoteDirectory()
            fileManager.createRemoteDirectory()
            clearLogcat()
        }
    }

    override fun dispose() {
        dispatcher.close()
        runBlocking {
            portForwardingRules.forEach { (_, rule) ->
                client.execute(RemoveReversePortForwardRequest(rule.localSpec), adbSerial)
            }
        }
    }

    fun executeTestRequest(runnerRequest: TestRunnerRequest): ReceiveChannel<List<TestEvent>> {
        return client.execute(runnerRequest, scope = this, serial = adbSerial)
    }

    private suspend fun setupReversePortForwarding() {
        val portForwarding = configuration.emulatorConfiguration.reversePortForwarding

        if (isLocalEmulator()) {
            if (portForwarding.gRPC) {
                val consolePort = adbSerial.substringAfter("emulator-").trim().toIntOrNull()
                if (consolePort == null) {
                    logger.debug { "Unable to parse emulator console port for serial $adbSerial" }
                    return
                }

                //Convention is to use (console port + 3000) as the gRPC port
                val gRPCPort = consolePort + 3000
                reversePortForward(
                    "gRPC",
                    ReversePortForwardingRule(adbSerial, RemoteTcpPortSpec(gRPCPort), LocalTcpPortSpec(gRPCPort))
                )
            }
            if (portForwarding.console) {
                val consolePort = adbSerial.substringAfter("emulator-").trim().toIntOrNull()
                if (consolePort == null) {
                    logger.debug { "Unable to parse emulator console port for serial $adbSerial" }
                    return
                }
                reversePortForward(
                    "console",
                    ReversePortForwardingRule(adbSerial, RemoteTcpPortSpec(consolePort), LocalTcpPortSpec(consolePort))
                )
            }
            if (portForwarding.adb) {
                reversePortForward(
                    "adb",
                    ReversePortForwardingRule(adbSerial, RemoteTcpPortSpec(client.port), LocalTcpPortSpec(client.port))
                )
            }
        }
    }

    private suspend fun reversePortForward(name: String, rule: ReversePortForwardingRule) {
        withTimeout(configuration.timeoutConfiguration.portForward) {
            client.execute(ReversePortForwardRequest(rule.localSpec, rule.remoteSpec), adbSerial)
            portForwardingRules[name] = rule
        }
    }
}
