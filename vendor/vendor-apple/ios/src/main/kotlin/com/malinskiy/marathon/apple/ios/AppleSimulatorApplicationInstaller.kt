package com.malinskiy.marathon.apple.ios

import com.malinskiy.marathon.apple.AppleApplicationInstaller
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.log.MarathonLogging

class AppleSimulatorApplicationInstaller(override val vendorConfiguration: VendorConfiguration.IOSConfiguration) : AppleApplicationInstaller<AppleSimulatorDevice>(vendorConfiguration) {
    private val logger = MarathonLogging.logger {}

    override suspend fun afterInstall(device: AppleSimulatorDevice) {
        grantPermissions(device)
    }

    private suspend fun grantPermissions(device: AppleSimulatorDevice) {
        val bundleId = vendorConfiguration.permissions.bundleId
        if (bundleId != null) {
            for (permission in vendorConfiguration.permissions.grant) {
                device.grant(permission, bundleId)
            }
        } else if (vendorConfiguration.permissions.grant.isNotEmpty()) {
            logger.warn { "Unable to grant permissions due to unknown bundle identifier" }
        }
    }

}
