package com.malinskiy.marathon.apple.ios.extensions

import com.malinskiy.marathon.apple.ios.model.AppleTestBundle
import com.malinskiy.marathon.config.vendor.VendorConfiguration

fun VendorConfiguration.IOSConfiguration.testBundle(): AppleTestBundle {
    val xctest = bundle?.xctest ?: throw IllegalArgumentException("No test bundle provided")
    val app = bundle?.app ?: throw IllegalArgumentException("No application bundle provided")

    return AppleTestBundle(app, xctest)
}
