package com.malinskiy.marathon.android.configuration

import com.fasterxml.jackson.annotation.JsonCreator

enum class SerialStrategy {
    AUTOMATIC,
    MARATHON_PROPERTY,
    BOOT_PROPERTY,
    HOSTNAME,
    DDMS;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(key: String?): SerialStrategy? {
            return key?.let {
                valueOf(it.toUpperCase())
            }
        }
    }
}
