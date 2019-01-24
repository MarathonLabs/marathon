package com.malinskiy.marathon.ios


import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandUnresponsiveException
import com.malinskiy.marathon.ios.cmd.remote.execOrNull
import com.malinskiy.marathon.ios.device.RemoteSimulator
import com.malinskiy.marathon.ios.device.RemoteSimulatorFeatureProvider
import com.malinskiy.marathon.ios.logparser.IOSDeviceLogParser
import com.malinskiy.marathon.ios.logparser.formatter.TestLogPackageNameFormatter
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.html.relativePathTo
import com.malinskiy.marathon.test.TestBatch
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.connection.channel.OpenFailException
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import net.schmizz.sshj.connection.ConnectionException
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

private const val COLLECT_LOGARCHIVES = false

class IOSDevice(val simulator: RemoteSimulator,
                simulatorSerial: Int,
                configuration: IOSConfiguration,
                val gson: Gson,
                private val healthChangeListener: HealthChangeListener): Device, CoroutineScope {

    val udid = simulator.udid
    val serial = "$udid-$simulatorSerial"
    private val deviceContext = newFixedThreadPoolContext(1, serial)

    override val coroutineContext: CoroutineContext
        get() = deviceContext

    val logger = MarathonLogging.logger(IOSDevice::class.java.simpleName)

    val hostCommandExecutor: SshjCommandExecutor

    val name: String?
    private val runtime: String?
    private val deviceType: String?

    init {
        hostCommandExecutor = try {
            SshjCommandExecutor(
                serial = serial,
                hostAddress = InetAddress.getByName(simulator.host),
                remoteUsername = simulator.username ?: configuration.remoteUsername,
                remotePrivateKey = configuration.remotePrivateKey,
                knownHostsPath = configuration.knownHostsPath,
                verbose = configuration.debugSsh
            )
        } catch(e: DeviceFailureException) {
            dispose()
            throw e
        }

        val simctl = Simctl()

        val device = try {
            simctl.list(this, gson).find { it.udid == udid }
        } catch (e: DeviceFailureException) {
            dispose()
            throw e
        }
        runtime = device?.runtime
        name = device?.name
        deviceType = simctl.deviceType(this)
    }

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem(runtime ?: "Unknown")
    override val serialNumber: String = udid

    override val model: String
        get() = deviceType ?: "Unknown"
    override val manufacturer: String
        get() = "Apple"
    override val networkState: NetworkState
        get() = when(healthy) {
            true -> NetworkState.CONNECTED
            false -> NetworkState.DISCONNECTED
        }
    override val deviceFeatures: Collection<DeviceFeature> by lazy {
        RemoteSimulatorFeatureProvider.deviceFeatures(this)
    }
    override var healthy: Boolean = true
        private set
    override val abi: String
        get() = "Simulator"

    var failureReason: DeviceFailureReason? = null
        private set

    private enum class Mode { STREAMING, XCRESULT }
    private val mode = Mode.STREAMING

    override suspend fun execute(configuration: Configuration,
                                 devicePoolId: DevicePoolId,
                                 testBatch: TestBatch,
                                 deferred: CompletableDeferred<TestBatchResults>,
                                 progressReporter: ProgressReporter) = withContext(deviceContext) {
        val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
        val fileManager = FileManager(configuration.outputDir)

        val remoteXcresultPath = RemoteFileManager.remoteXcresultFile(this@IOSDevice)
        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this@IOSDevice)
        val remoteDir = remoteXctestrunFile.parent

        logger.debug("Remote xctestrun = $remoteXctestrunFile")

        val xctestrun = Xctestrun(iosConfiguration.xctestrunPath)
        val packageNameFormatter = TestLogPackageNameFormatter(xctestrun.productModuleName, xctestrun.targetName)

        logger.debug("Tests = ${testBatch.tests.toList()}")

        val logParser = IOSDeviceLogParser(this@IOSDevice,
            packageNameFormatter,
            devicePoolId,
            testBatch,
            deferred,
            progressReporter,
            iosConfiguration.hideRunnerOutput
        )

        val command =
                listOfNotNull("xcrun simctl shutdown $udid;",
                        "xcrun simctl erase $udid;",
                        "cd '$remoteDir';",
                        "NSUnbufferedIO=YES",
                        "xcodebuild test-without-building",
                        "-xctestrun ${remoteXctestrunFile.path}",
                        if (mode == Mode.XCRESULT) "-resultBundlePath ${remoteXcresultPath.canonicalPath} " else null,
                        testBatch.toXcodebuildArguments(),
                        "-destination 'platform=iOS simulator,id=$udid' ;",
                        "exit")
                        .joinToString(" ")
                        .also { logger.debug("\u001b[1m$it\u001b[0m") }

        val exitStatus = try {
            hostCommandExecutor.exec(
                command,
                configuration.timeoutMillis,
                configuration.testOutputTimeoutMillis,
                logParser::onLine
            )
        } catch (e: SshjCommandUnresponsiveException) {
            logger.error("No output from remote shell")
            disconnectAndNotify()
            throw DeviceLostException(e)
        } catch (e: TimeoutException) {
            logger.error("Connection timeout")
            disconnectAndNotify()
            throw DeviceLostException(e)
        } catch (e: ConnectionException) {
            logger.error("ConnectionException")
            disconnectAndNotify()
            throw DeviceLostException(e)
        } catch (e: TransportException) {
            logger.error("TransportException")
            disconnectAndNotify()
            throw DeviceLostException(e)
        } catch (e: OpenFailException) {
            logger.error("Unable to open session")
            disconnectAndNotify()
            throw DeviceLostException(e)
        } catch (e: IllegalStateException) {
            logger.error("Unable to start a new SSH session. Client is disconnected")
            disconnectAndNotify()
            throw DeviceLostException(e)
        } catch(e: DeviceFailureException) {
            logger.error("$e")
            failureReason = e.reason
            disconnectAndNotify()
            throw DeviceLostException(e)
        } finally {
            logParser.close()

            if (!healthy) {
                logger.debug("Last log before device termination")
                logger.debug(logParser.getLastLog())
            }

            if (logParser.diagnosticLogPaths.isNotEmpty())
                logger.info("Diagnostic logs available at ${logParser.diagnosticLogPaths}")

            if (logParser.sessionResultPaths.isNotEmpty())
                logger.info("Session results available at ${logParser.sessionResultPaths}")
        }

        // 70 = no devices
        // 65 = ** TEST EXECUTE FAILED **: crash
        logger.debug("Finished test batch execution with exit status $exitStatus")
    }
    private suspend fun disconnectAndNotify() {
        healthy = false
        healthChangeListener.onDisconnect(this)
    }

    private var derivedDataManager: DerivedDataManager? = null
    override suspend fun prepare(configuration: Configuration) = withContext(coroutineContext) {
        RemoteFileManager.createRemoteDirectory(this@IOSDevice)

        val derivedDataManager = DerivedDataManager(configuration)

        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this@IOSDevice)
        val xctestrunFile = prepareXctestrunFile(derivedDataManager, remoteXctestrunFile)

        derivedDataManager.sendSynchronized(
            localPath = xctestrunFile,
            remotePath = remoteXctestrunFile.absolutePath,
            hostName = hostCommandExecutor.hostAddress.hostName,
            port = hostCommandExecutor.port
        )

        derivedDataManager.sendSynchronized(
            localPath = derivedDataManager.productsDir,
            remotePath = RemoteFileManager.remoteDirectory(this@IOSDevice).path,
            hostName = hostCommandExecutor.hostAddress.hostName,
            port = hostCommandExecutor.port
        )

        this@IOSDevice.derivedDataManager = derivedDataManager

        val result = try {
            hostCommandExecutor.exec("/usr/libexec/PlistBuddy -c 'Add :DevicePreferences:$udid:ConnectHardwareKeyboard bool false' /Users/master/Library/Preferences/com.apple.iphonesimulator.plist" +
                    "|| /usr/libexec/PlistBuddy -c 'Set :DevicePreferences:$udid:ConnectHardwareKeyboard false' /Users/master/Library/Preferences/com.apple.iphonesimulator.plist")
        } catch (e: Exception) {
            null
        }
        if (result?.exitStatus == 0) {
            logger.debug("Disabled hardware keyboard")
        } else if (result?.stderr != null) {
            logger.debug("Failed to disable hardware keyboard with error: ${result.stderr}")
        } else {
            logger.debug("Failed to disable hardware keyboard")
        }
    }

    private val disposing = AtomicBoolean(false)
    override fun dispose() {
        if (disposing == null // interrupted initialization
                || disposing.compareAndSet(false, true)) {
            collectLogarchives()
            hostCommandExecutor.disconnect()
            deviceContext.close()
        }
    }

    override fun toString(): String {
        return "IOSDevice"
    }

    private val deviceIdentifier: String
        get() = "${hostCommandExecutor.hostAddress.hostAddress}:$udid"

    private fun prepareXctestrunFile(derivedDataManager: DerivedDataManager, remoteXctestrunFile: File): File {
        val remotePort = RemoteSimulatorFeatureProvider.availablePort(this)
                .also { logger.info("Using TCP port $it on device $deviceIdentifier") }

        val xctestrun = Xctestrun(derivedDataManager.xctestrunFile)
        xctestrun.environment("TEST_HTTP_SERVER_PORT", "$remotePort")

        return derivedDataManager.xctestrunFile.
                resolveSibling(remoteXctestrunFile.name)
                .also { it.writeBytes(xctestrun.toXMLByteArray()) }
    }

    private fun collectLogarchives() {
        if (!COLLECT_LOGARCHIVES) { return }

        if (healthy) { return }

        val logarchiveFile = RemoteFileManager.remoteLogarchiveFile(this)
        val result = hostCommandExecutor.execOrNull(
                "xcrun simctl boot $udid; sleep 1; rm -Rf '$logarchiveFile'; xcrun simctl spawn $udid log collect --output '$logarchiveFile'",
                60000L, 60000L
            )
        when {
            result?.exitStatus == 0 -> {
                logger.debug("Collected logarchive to $logarchiveFile")
                try {
                    derivedDataManager?.let {
                        val localLogarchive = it.productsDir.resolve(logarchiveFile.name)
                        if (it.receive(
                                    remotePath = logarchiveFile.absolutePath,
                                    hostName = hostCommandExecutor.hostAddress.hostName,
                                    port = hostCommandExecutor.port,
                                    localPath = localLogarchive
                                ) == 0) {
                            logger.debug("Downloaded logarchive to ${localLogarchive.absoluteFile}")
                            val iosConfiguration = it.configuration.vendorConfiguration as IOSConfiguration
                            if (iosConfiguration.teamcityCheckoutDir != null) {
                                val artifactPath = localLogarchive.relativePathTo(iosConfiguration.teamcityCheckoutDir)
                                logger.info("##teamcity[publishArtifacts '$artifactPath => logarchives/${localLogarchive.name}.zip']")
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
            result?.stderr != null -> logger.debug("Failed to collect logarchive $logarchiveFile: ${result.stderr}")
            else -> logger.debug("Failed to collect logarchive $logarchiveFile")
        }
    }
}

private fun TestBatch.toXcodebuildArguments(): String = tests.joinToString(separator = " ") { "-only-testing:\"${it.pkg}/${it.clazz}/${it.method}\"" }
