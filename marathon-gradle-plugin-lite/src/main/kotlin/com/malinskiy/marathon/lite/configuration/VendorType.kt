package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration

enum class VendorType {
    DDMLIB,
    ADAM
}

fun VendorType.toProto() {
    when (this) {
        VendorType.DDMLIB -> AndroidConfiguration.VendorType.DDMLIB
        VendorType.ADAM -> AndroidConfiguration.VendorType.ADAM
    }
}
