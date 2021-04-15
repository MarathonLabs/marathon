package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration

enum class SerialStrategy {
    AUTOMATIC,
    MARATHON_PROPERTY,
    BOOT_PROPERTY,
    HOSTNAME,
    DDMS;
}

fun SerialStrategy.toProto(): AndroidConfiguration.SerialStrategy {
    return when (this) {
        SerialStrategy.AUTOMATIC -> AndroidConfiguration.SerialStrategy.AUTOMATIC
        SerialStrategy.MARATHON_PROPERTY -> AndroidConfiguration.SerialStrategy.MARATHON_PROPERTY
        SerialStrategy.BOOT_PROPERTY -> AndroidConfiguration.SerialStrategy.BOOT_PROPERTY
        SerialStrategy.HOSTNAME -> AndroidConfiguration.SerialStrategy.HOSTNAME
        SerialStrategy.DDMS -> AndroidConfiguration.SerialStrategy.DDMS
    }
}
