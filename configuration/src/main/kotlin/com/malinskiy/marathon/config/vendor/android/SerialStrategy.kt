package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.Locale

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
                valueOf(it.uppercase(Locale.ENGLISH))
            }
        }
    }
}
