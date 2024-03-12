package com.malinskiy.marathon.apple.macos

import com.malinskiy.marathon.apple.AppleApplicationInstaller
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.exceptions.IncompatibleDeviceException

class MacosApplicationInstaller(override val vendorConfiguration: VendorConfiguration.MacosConfiguration) :
    AppleApplicationInstaller<MacosDevice>(vendorConfiguration) {
    override suspend fun afterInstall(device: MacosDevice, bundle: AppleTestBundle) {
        grantPermissions(device)
    }

    private suspend fun grantPermissions(device: MacosDevice) {
        if (vendorConfiguration.permissions.grant.isEmpty()) return

        if (device.binaryEnvironment.csrutil.status()) {
            throw IncompatibleDeviceException("Unable to add permissions: requires System Integrity Protection to be disabled")
        }
        for ((client, permission) in vendorConfiguration.permissions.grant.entries) {
            device.grant(client, permission)
        }
    }
}
