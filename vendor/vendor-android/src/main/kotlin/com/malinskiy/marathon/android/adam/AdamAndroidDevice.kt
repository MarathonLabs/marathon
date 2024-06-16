package com.malinskiy.marathon.android.adam

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.exception.PullFailedException
import com.malinskiy.adam.exception.PushFailedException
import com.malinskiy.adam.exception.RequestRejectedException
import com.malinskiy.adam.exception.UnsupportedImageProtocolException
import com.malinskiy.adam.exception.UnsupportedSyncProtocolException
import com.malinskiy.adam.request.Feature
import com.malinskiy.adam.request.device.DeviceState
import com.malinskiy.adam.request.device.FetchDeviceFeaturesRequest
import com.malinskiy.adam.request.forwarding.LocalTcpPortSpec
import com.malinskiy.adam.request.forwarding.RemoteTcpPortSpec
import com.malinskiy.adam.request.framebuffer.BufferedImageScreenCaptureAdapter
import com.malinskiy.adam.request.framebuffer.ScreenCaptureRequest
import com.malinskiy.adam.request.pkg.InstallRemotePackageRequest
import com.malinskiy.adam.request.pkg.InstallSplitPackageRequest
import com.malinskiy.adam.request.pkg.UninstallRemotePackageRequest
import com.malinskiy.adam.request.pkg.multi.ApkSplitInstallationPackage
import com.malinskiy.adam.request.prop.GetPropRequest
import com.malinskiy.adam.request.reverse.RemoveReversePortForwardRequest
import com.malinskiy.adam.request.reverse.ReversePortForwardRequest
import com.malinskiy.adam.request.reverse.ReversePortForwardingRule
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import com.malinskiy.adam.request.sync.AndroidFile
import com.malinskiy.adam.request.sync.AndroidFileType
import com.malinskiy.adam.request.sync.ListFilesRequest
import com.malinskiy.adam.request.sync.PushRequest
import com.malinskiy.adam.request.sync.compat.CompatPullFileRequest
import com.malinskiy.adam.request.sync.compat.CompatPushFileRequest
import com.malinskiy.adam.request.sync.compat.CompatStatFileRequest
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.BaseAndroidDevice
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.adam.extension.toShellResult
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.exception.InstallException
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorder.Companion.addFileNumberForVideo
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.android.extension.toScreenRecorderCommand
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import com.malinskiy.marathon.coroutines.newCoroutineExceptionHandler
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.file.measureFileTransfer
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.extension.withTimeout
import com.malinskiy.marathon.extension.withTimeoutOrNull
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import com.malinskiy.marathon.android.model.ShellCommandResult as MarathonShellCommandResult

class AdamAndroidDevice(
    internal val client: AndroidDebugBridgeClient,
    private val deviceStateTracker: DeviceStateTracker,
    private val logcatManager: LogcatManager,
    private val testBundleIdentifier: AndroidTestBundleIdentifier,
    private val installContext: CoroutineContext,
    adbSerial: String,
    configuration: Configuration,
    androidConfiguration: VendorConfiguration.AndroidConfiguration,
    track: Track,
    timer: Timer,
    serialStrategy: SerialStrategy
) : BaseAndroidDevice(adbSerial, serialStrategy, configuration, androidConfiguration, track, timer), LineListener {

    /**
     * This adapter is thread-safe but the internal reusable buffer should be considered if we ever need to make screenshots in parallel
     */
    private val imageScreenCaptureAdapter = BufferedImageScreenCaptureAdapter()
    lateinit var supportedFeatures: List<Feature>

    val portForwardingRules = mutableMapOf<String, ReversePortForwardingRule>()

    override val serialNumber: String
        get() = when {
            booted -> realSerialNumber
            else -> "${client.host.hostAddress}:${client.port}:$adbSerial"
        }

    override suspend fun detectRealSerialNumber() = "${client.host.hostAddress}:${client.port}:${super.detectRealSerialNumber()}"

    override suspend fun setup() {
        withContext(coroutineContext) {
            super.setup()

            fetchProps()
            supportedFeatures = client.execute(FetchDeviceFeaturesRequest(adbSerial))
            logcatManager.subscribe(this@AdamAndroidDevice)
            setupTestAccess()
        }
    }

    private val dispatcher by lazy {
        newFixedThreadPoolContext(2, "AndroidDevice - execution - ${client.host.hostAddress}:${client.port}:${adbSerial}")
    }

    override val coroutineContext = dispatcher + newCoroutineExceptionHandler(logger)

    private var props: Map<String, String> = emptyMap()

    override suspend fun executeShellCommand(command: String, errorMessage: String): MarathonShellCommandResult? {
        return try {
            return client.execute(ShellCommandRequest(command), serial = adbSerial).toShellResult()
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    override suspend fun safeExecuteShellCommand(command: String, errorMessage: String): MarathonShellCommandResult? {
        return try {
            withTimeoutOrNull(androidConfiguration.timeoutConfiguration.shell) {
                client.execute(ShellCommandRequest(command), serial = adbSerial).toShellResult()
            }
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    override suspend fun criticalExecuteShellCommand(command: String, errorMessage: String): MarathonShellCommandResult {
        return withTimeoutOrNull(androidConfiguration.timeoutConfiguration.shell) {
            client.execute(ShellCommandRequest(command), serial = adbSerial).toShellResult()
        } ?: throw CommandRejectedException(errorMessage)
    }

    override suspend fun pullFile(remoteFilePath: String, localFilePath: String) {
        var progress: Double = 0.0
        try {
            val local = File(localFilePath)

            measureFileTransfer(local) {
                val stat = client.execute(CompatStatFileRequest(remoteFilePath, supportedFeatures), adbSerial)
                when {
                    stat.exists() && stat.size() > 0.toULong() -> {
                        val channel = client.execute(
                            CompatPullFileRequest(
                                remoteFilePath,
                                local,
                                supportedFeatures,
                                coroutineScope = this,
                                size = stat.size().toLong()
                            ),
                            serial = adbSerial
                        )
                        for (update in channel) {
                            progress = update
                        }
                    }

                    stat.exists() && stat.size() == 0.toULong() -> {
                        local.createNewFile()
                        progress = 1.0
                    }

                    else -> throw TransferException("Couldn't pull file $remoteFilePath from device $serialNumber because it doesn't exist")
                }
            }
        } catch (e: PullFailedException) {
            throw TransferException("Couldn't pull file $remoteFilePath from device $serialNumber", e)
        } catch (e: UnsupportedSyncProtocolException) {
            throw TransferException("Device $serialNumber does not support sync: file transfer", e)
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
            measureFileTransfer(File(localFilePath)) {
                val channel = client.execute(
                    CompatPushFileRequest(
                        file,
                        remoteFilePath,
                        supportedFeatures,
                        coroutineScope = this,
                        mode = "0777",
                        coroutineContext = installContext
                    ),
                    serial = adbSerial
                )
                for (update in channel) {
                    progress = update
                }
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
                withTimeoutOrNull(androidConfiguration.timeoutConfiguration.listFiles) {
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
            val remoteFilePath = "${file.directory}/${file.name}"

            withTimeoutOrNull(androidConfiguration.timeoutConfiguration.pullFile) {
                pullFile(remoteFilePath, localFile.absolutePath)
            } ?: logger.warn { "Pulling $remoteFilePath timed out. Ignoring" }
        }
    }

    override suspend fun pushFolder(localFolderPath: String, remoteFolderPath: String) {
        if (!File(localFolderPath).isDirectory) {
            throw TransferException("Source $localFolderPath is not a directory")
        }
        withTimeoutOrNull(androidConfiguration.timeoutConfiguration.pushFolder) {
            client.execute(
                PushRequest(File(localFolderPath), remoteFolderPath, supportedFeatures, coroutineContext = dispatcher),
                serial = adbSerial
            )
        } ?: logger.warn { "Pushing $localFolderPath timed out. Ignoring" }
    }

    override suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean): MarathonShellCommandResult? {
        return withTimeoutOrNull(androidConfiguration.timeoutConfiguration.uninstall) {
            client.execute(UninstallRemotePackageRequest(appPackage, keepData = keepData), serial = adbSerial).toShellResult()
        }
    }

    override suspend fun installPackage(
        absolutePath: String,
        reinstall: Boolean,
        optionalParams: List<String>
    ): MarathonShellCommandResult {
        val file = File(absolutePath)
        //Very simple escaping for the name of the file
        val fileName = file.name.escape()
        val remotePath = "${RemoteFileManager.TMP_PATH}/$fileName"

        try {
            withTimeoutOrNull(androidConfiguration.timeoutConfiguration.pushFile) {
                pushFile(absolutePath, remotePath, verify = true)
            } ?: throw InstallException("Timeout transferring $absolutePath")
        } catch (e: TransferException) {
            throw InstallException(e)
        }

        val result = withTimeoutOrNull(androidConfiguration.timeoutConfiguration.install) {
            client.execute(
                InstallRemotePackageRequest(
                    remotePath,
                    reinstall = reinstall,
                    extraArgs = optionalParams.filter { it.isNotBlank() }
                ), serial = adbSerial
            )
        } ?: throw InstallException("Timeout transferring $absolutePath")

        safeExecuteShellCommand("rm $remotePath")
        return com.malinskiy.marathon.android.model.ShellCommandResult(result.output.trim(), result.exitCode)
    }

    override suspend fun installSplitPackages(absolutePaths: List<String>, reinstall: Boolean, optionalParams: List<String>): String {
        return withTimeoutOrNull(androidConfiguration.timeoutConfiguration.install) {
            val files = absolutePaths.map { File(it) }
            client.execute(
                InstallSplitPackageRequest(
                    pkg = ApkSplitInstallationPackage(files),
                    supportedFeatures = listOf(Feature.CMD),
                    reinstall = reinstall,
                    extraArgs = optionalParams,
                    coroutineContext = Dispatchers.IO
                ), serial = adbSerial
            )
        } ?: throw InstallException("Timeout transferring absolutePath")
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        return try {
            withTimeoutOrNull(timeout) {
                client.execute(ScreenCaptureRequest(imageScreenCaptureAdapter), serial = adbSerial)
            }
        } catch (e: UnsupportedImageProtocolException) {
            logger.warn(e) { "Unable to retrieve screenshot from device $serialNumber" }
            null
        }
    }

    private val logcatListeners = CopyOnWriteArrayList<LineListener>()

    override fun addLineListener(listener: LineListener) {
        logcatListeners.add(listener)
    }

    override fun removeLineListener(listener: LineListener) {
        logcatListeners.remove(listener)
    }

    override suspend fun safeStartScreenRecorder(
        remoteFilePath: String,
        options: VideoConfiguration
    ) {
        var secondsRemaining = TimeUnit.SECONDS.convert(options.timeLimit, options.timeLimitUnits)
        if(secondsRemaining > 180 && apiLevel < 34) {
            var recordsCount = 0L
            while(recordsCount == 0L || secondsRemaining >= 180) {
                startScreenRecorder(remoteFilePath, options, recordsCount) {
                    secondsRemaining -= 180
                    recordsCount++
                }
            }
        } else {
            startScreenRecorder(remoteFilePath, options)
        }
    }

    private suspend fun startScreenRecorder(
        remoteFilePath: String,
        options: VideoConfiguration,
        counter: Long? = null,
        recordFinished: (() -> Unit)? = null
    ) {
        val screenRecorderCommand = options.toScreenRecorderCommand(remoteFilePath.addFileNumberForVideo(counter), this)
        try {
            withTimeoutOrNull(androidConfiguration.timeoutConfiguration.screenrecorder) {
                val result = client.execute(ShellCommandRequest(screenRecorderCommand), serial = adbSerial)
                logger.debug {
                    StringBuilder().apply {
                        append("screenrecord result: ")
                        if (result.output.isNotBlank()) append("output='${result.output}'")
                        append("exit code=${result.exitCode}")
                    }.toString()
                }
            }
        } catch (e: CancellationException) {
            logger.warn(e) { "screenrecord start was interrupted" }
        } catch (e: Exception) {
            logger.error("Unable to start screenrecord", e)
        } finally {
            recordFinished?.invoke()
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
        deferred: CompletableDeferred<TestBatchResults>
    ) {
        var job: Job? = null
        try {
            job = async(coroutineContext + CoroutineName("execute $serialNumber")) {
                supervisorScope {
                    val listener = createExecutionListeners(configuration, devicePoolId, testBatch, deferred)
                    AndroidDeviceTestRunner(this@AdamAndroidDevice, testBundleIdentifier).execute(
                        configuration,
                        testBatch,
                        listener
                    )
                }
            }
            job.await()
        } catch (e: RequestRejectedException) {
            throw DeviceLostException(e)
        } catch (e: CommandRejectedException) {
            throw DeviceLostException(e)
        } catch (e: CancellationException) {
            job?.cancel(e)
            throw e
        }
    }

    override suspend fun prepare(configuration: Configuration) {
        async(coroutineContext) {
            supervisorScope {

                track.trackDevicePreparing(this@AdamAndroidDevice) {
                    AndroidAppInstaller(configuration).prepareInstallation(this@AdamAndroidDevice)

                    fileManager.removeRemoteDirectory()
                    fileManager.createRemoteDirectory()

                    clearLogcat()
                }
            }
        }.await()
    }

    override fun dispose() {
        dispatcher.close()
        logcatManager.unsubscribe(this)
        runBlocking {
            portForwardingRules.forEach { (_, rule) ->
                client.execute(RemoveReversePortForwardRequest(rule.localSpec), adbSerial)
            }
        }
    }

    suspend fun executeTestRequest(runnerRequest: TestRunnerRequest): ReceiveChannel<List<TestEvent>> {
        return client.execute(runnerRequest, serial = adbSerial)
    }

    override suspend fun onLine(line: String) {
        logcatListeners.forEach { listener ->
            listener.onLine(line)
        }
    }

    private suspend fun setupTestAccess() {
        val accessConfiguration = androidConfiguration.testAccessConfiguration

        if (accessConfiguration.adb && !isLocalEmulator()) {
            reversePortForward(
                "adb",
                ReversePortForwardingRule(adbSerial, RemoteTcpPortSpec(client.port), LocalTcpPortSpec(client.port))
            )
        }
    }

    private suspend fun reversePortForward(name: String, rule: ReversePortForwardingRule) {
        withTimeout(androidConfiguration.timeoutConfiguration.portForward) {
            client.execute(ReversePortForwardRequest(rule.localSpec, rule.remoteSpec), adbSerial)
            portForwardingRules[name] = rule
        }
    }
}
