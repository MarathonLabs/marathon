package com.malinskiy.marathon.ios


import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.ios.device.RemoteSimulatorFeatureProvider
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class IOSDevice(
    connectionAttempt: Int,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    val gson: Gson,
    private val track: Track,
    private val healthChangeListener: HealthChangeListener,
    private val timer: Timer
) : Device, CoroutineScope {

    override val networkState: NetworkState
        get() = when (healthy) {
            true -> NetworkState.CONNECTED
            false -> NetworkState.DISCONNECTED
        }
    override val deviceFeatures: Collection<DeviceFeature> by lazy {
        RemoteSimulatorFeatureProvider.deviceFeatures(this)
    }
    
    var failureReason: DeviceFailureReason? = null
        private set

    

    private suspend fun disconnectAndThrow(cause: Throwable) {
        healthy = false
        healthChangeListener.onDisconnect(this)
        throw DeviceLostException(cause)
    }

    private lateinit var derivedDataManager: DerivedDataManager
    override suspend fun prepare(configuration: Configuration) = withContext(coroutineContext + CoroutineName("prepare")) {
        val iosConfiguration = configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration

        track.trackDevicePreparing(this@IOSDevice) {
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

            iosConfiguration.derivedDataDir.resolve("Build/Products")
            derivedDataManager.sendSynchronized(
                localPath = derivedDataManager.productsDir,
                remotePath = RemoteFileManager.remoteDirectory(this@IOSDevice).path,
                hostName = hostCommandExecutor.hostAddress.hostName,
                port = hostCommandExecutor.port
            )

            this@IOSDevice.derivedDataManager = derivedDataManager

            terminateRunningSimulators()
            if (!iosConfiguration.alwaysEraseSimulators) {
                try {
                    
                } catch (e: Exception) {
                    logger.warn("Exception shutting down remote simulator $e")
                }
                try {
                    hostCommandExecutor.exec(
                        "xcrun simctl erase $udid",
                        configuration.testBatchTimeoutMillis,
                        configuration.testOutputTimeoutMillis
                    )
                } catch (e: Exception) {
                    logger.warn("Exception erasing remote simulator $e")
                }
            }
            disableHardwareKeyboard()
        }
    }

    //    suspend fun sendSynchronized(localPath: File, remotePath: String, hostName: String, port: Int) {
//        hostnameLocksMap.getOrPut(hostName) { ReentrantLock() }.withLock {
//            send(localPath, remotePath, hostName, port)
//        }
//    }

    private fun terminateRunningSimulators() {
        val result = hostCommandExecutor.execOrNull("/usr/bin/pkill -9 -l -f '$udid'")
        if (result?.exitCode == 0) {
            logger.trace("Terminated loaded simulators")
        } else {
            logger.debug("Failed to terminate loaded simulators ${result?.stdout}")
        }

        val ps = hostCommandExecutor.execOrNull("/bin/ps | /usr/bin/grep '$udid'")?.stdout ?: ""
        if (ps.isNotBlank()) {
            logger.debug(ps)
        }
    }

    private fun disableHardwareKeyboard() {
        val result =
            hostCommandExecutor.execOrNull(
                "/usr/libexec/PlistBuddy -c 'Add :DevicePreferences:$udid:ConnectHardwareKeyboard bool false' /Users/master/Library/Preferences/com.apple.iphonesimulator.plist" +
                    "|| /usr/libexec/PlistBuddy -c 'Set :DevicePreferences:$udid:ConnectHardwareKeyboard false' /Users/master/Library/Preferences/com.apple.iphonesimulator.plist"
            )
        if (result?.exitCode == 0) {
            logger.trace("Disabled hardware keyboard")
        } else {
            logger.debug("Failed to disable hardware keyboard ${result?.stdout}")
        }
    }

    override fun dispose() {
        logger.debug("Disposing device")
        try {
            hostCommandExecutor.close()
        } catch (e: Exception) {
            logger.debug("Error disconnecting ssh: $e")
        }

        try {
            deviceContext.close()
        } catch (e: Exception) {
            logger.debug("Error closing context: $e")
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

        return derivedDataManager.xctestrunFile.resolveSibling(remoteXctestrunFile.name)
            .also { it.writeBytes(xctestrun.toXMLByteArray()) }
    }

    fun availablePort(): Int {
        val commandResult = hostCommandExecutor.execBlocking(
            """ruby -e 'require "socket"; puts Addrinfo.tcp("", 0).bind {|s| s.local_address.ip_port }'"""
        )
        return when {
            commandResult.exitCode == 0 -> commandResult.stdout.trim().toIntOrNull()
            else -> null
        } ?: throw SshjCommandException(commandResult.stdout)
    }

    
}
