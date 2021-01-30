package com.malinskiy.marathon.android.configuration

enum class SerialStrategy {
    AUTOMATIC,
    MARATHON_PROPERTY,
    BOOT_PROPERTY,
    HOSTNAME,
    DDMS;
}
