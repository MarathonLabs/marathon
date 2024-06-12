package com.malinskiy.marathon.apple.ios

import com.malinskiy.marathon.apple.AppleApplicationInstaller
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging

class AppleSimulatorMediaImporter(override val vendorConfiguration: VendorConfiguration.IOSConfiguration) :
    AppleApplicationInstaller<AppleSimulatorDevice>(vendorConfiguration) {

    private val logger = MarathonLogging.logger {}

    suspend fun importMedia(device: AppleSimulatorDevice) {
        if (vendorConfiguration.mediaFiles.isNullOrEmpty()) return

        device.remoteFileManager.createRemoteSharedMediaDirectory()

        vendorConfiguration.mediaFiles?.forEach { mFile ->
            val remoteMediaFile = device.remoteFileManager.remoteMediaFile(mFile.name)

            if (!device.pushFile(mFile, remoteMediaFile)) {
                throw DeviceSetupException("Error transferring $remoteMediaFile to ${device.serialNumber}")
            }
            withRetry(3, 1000L) {
                logger.debug { "Importing media $mFile to ${device.serialNumber}" }
                if (!device.importMedia(remoteMediaFile)) {
                    throw DeviceSetupException("Error importing media $remoteMediaFile to ${device.serialNumber}")
                }
            }
        }
    }
}
