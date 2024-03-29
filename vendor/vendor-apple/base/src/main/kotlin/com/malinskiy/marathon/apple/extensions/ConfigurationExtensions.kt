package com.malinskiy.marathon.apple.extensions

import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.AppleTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.apple.ios.XcresultConfiguration
import com.malinskiy.marathon.config.vendor.apple.ios.XctestrunEnvConfiguration

fun VendorConfiguration.bundleConfiguration(): AppleTestBundleConfiguration? {
    return when (this) {
        is VendorConfiguration.IOSConfiguration -> {
            this.bundle
        }

        is VendorConfiguration.MacosConfiguration -> {
            this.bundle
        }

        else -> null
    }
}

fun VendorConfiguration.xctestrunEnv(): XctestrunEnvConfiguration? {
    return when (this) {
        is VendorConfiguration.IOSConfiguration -> {
            this.xctestrunEnv
        }

        is VendorConfiguration.MacosConfiguration -> {
            this.xctestrunEnv
        }

        else -> null
    }
}

fun VendorConfiguration.xcresultConfiguration(): XcresultConfiguration? {
    return when (this) {
        is VendorConfiguration.IOSConfiguration -> {
            this.xcresult
        }

        is VendorConfiguration.MacosConfiguration -> {
            this.xcresult
        }

        else -> null
    }
}
