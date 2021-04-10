package com.malinskiy.marathon.cli.schema.android

enum class SerialStrategy {
    AUTOMATIC,
    MARATHON_PROPERTY,
    BOOT_PROPERTY,
    HOSTNAME,
    DDMS;
}
