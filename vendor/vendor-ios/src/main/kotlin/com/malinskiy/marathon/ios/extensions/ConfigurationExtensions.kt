package com.malinskiy.marathon.ios.extensions

import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.ios.model.AppleTestBundle

fun VendorConfiguration.IOSConfiguration.testBundle(): AppleTestBundle {
    val xctest = bundle?.xctest ?: throw IllegalArgumentException("No test bundle provided")
    val app = bundle?.app ?: throw IllegalArgumentException("No application bundle provided")

    return AppleTestBundle(app, xctest)
}
