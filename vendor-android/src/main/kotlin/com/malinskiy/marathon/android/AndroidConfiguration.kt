package com.malinskiy.marathon.android

import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class AndroidConfiguration(
    val androidSdk: File,
    val adbInitTimeoutMillis: Int = 30_000
) : VendorConfiguration