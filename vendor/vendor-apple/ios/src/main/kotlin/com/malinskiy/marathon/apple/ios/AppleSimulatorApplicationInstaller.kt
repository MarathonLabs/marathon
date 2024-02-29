package com.malinskiy.marathon.apple.ios

import com.malinskiy.marathon.apple.AppleApplicationInstaller
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.log.MarathonLogging

class AppleSimulatorApplicationInstaller(override val vendorConfiguration: VendorConfiguration.IOSConfiguration) :
    AppleApplicationInstaller<AppleSimulatorDevice>(vendorConfiguration) {

    override suspend fun afterInstall(device: AppleSimulatorDevice, bundle: AppleTestBundle) {
        grantPermissions(device, bundle.appId)
    }

    private suspend fun grantPermissions(device: AppleSimulatorDevice, bundleId: String) {
        for (permission in vendorConfiguration.permissions.grant) {
            device.grant(permission, bundleId)
        }
    }
}
