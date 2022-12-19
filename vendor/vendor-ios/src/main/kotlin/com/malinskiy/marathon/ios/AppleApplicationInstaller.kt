package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

private const val PRODUCTS_PATH = "Build/Products"

class AppleApplicationInstaller(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration
) {
    private val logger = MarathonLogging.logger {}
    
    suspend fun prepareInstallation(device: AppleSimulatorDevice) {
        logger.debug { "Preparing xctestrun for ${device.serialNumber}" }
        val preparedXctestrun = prepareXctestrun(device)
        
        logger.debug { "Moving xctestrun to ${device.serialNumber}" }
        val remoteXctestrunFile = device.remoteFileManager.remoteXctestrunFile()
        withRetry(3, 1000L) {
            if(!device.pushFile(preparedXctestrun, remoteXctestrunFile)) {
                throw DeviceSetupException("Error transferring ${vendorConfiguration.safecxtestrunPath()} to ${device.serialNumber}")
            }
        }

        logger.debug { "Moving $PRODUCTS_PATH to ${device.serialNumber}" }
        withRetry(3, 1000L) {
            val productsDir = vendorConfiguration.derivedDataDir.resolve(PRODUCTS_PATH)
            val remoteDirectory = device.remoteFileManager.remoteDirectory()
            if(!device.pushFolder(productsDir, remoteDirectory)) {
                throw DeviceSetupException("Error transferring $productsDir to ${device.serialNumber}")
            }
        }
    }

    private fun prepareXctestrun(device: AppleSimulatorDevice): File {
        val xctestrunPath = vendorConfiguration.safecxtestrunPath()
        val xctestrun = Xctestrun(xctestrunPath).apply {
            environment(vendorConfiguration.xctestrunEnv)
        }
        val preparedXctestrun = xctestrunPath.resolveSibling(device.remoteFileManager.xctestrunFileName())
            .also { it.writeBytes(xctestrun.toXMLByteArray()) }
        return preparedXctestrun
    }
    
    /**
     * This is some Agoda feature code that preallocates an open port for mock server. 
     * While I understand the allocation of a port on a remote machine requires ssh'ing into remote machine,
     * There is probably a better solution for this. E.g. passing environment variables to xctestrun without any preallocation
     */
    suspend fun availablePort(device: AppleSimulatorDevice): Int? {
        val commandResult = device.executeWorkerCommand(
            listOf(
                "ruby",
                "-e",
                "'require \"socket\"; puts Addrinfo.tcp(\"\", 0).bind {|s| s.local_address.ip_port }'"
            )
        )
        return when {
            commandResult?.exitCode == 0 -> commandResult.combinedStdout.trim().toIntOrNull()
            else -> null
        }
    }
}
