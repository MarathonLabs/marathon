package com.malinskiy.marathon.device

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.Locale

enum class DeviceFeature {
    VIDEO,
    SCREENSHOT;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(key: String?): DeviceFeature? {
            return key?.let {
                DeviceFeature.valueOf(it.uppercase(Locale.ENGLISH))
            }
        }
    }
}
