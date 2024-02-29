package com.malinskiy.marathon.apple.model

import com.malinskiy.marathon.exceptions.IncompatibleDeviceException

class AppleOperatingSystemVersion(value: String) : Comparable<AppleOperatingSystemVersion> {
    val major: Int
    val minor: Int
    val patch: Int

    init {
        val split = value.split(".")
        if (split.isEmpty() || split.size > 3) {
            throw IncompatibleDeviceException("Unknown operating system version $value")
        }
        major = split.getOrNull(0)?.toIntOrNull() ?: throw IncompatibleDeviceException("Unable to parser operating system version $value")
        minor = split.getOrNull(1)?.toIntOrNull() ?: 0
        patch = split.getOrNull(2)?.toIntOrNull() ?: 0
    }

    override fun compareTo(other: AppleOperatingSystemVersion): Int {
        if (major.compareTo(other.major) != 0) return major.compareTo(other.major)
        if (minor.compareTo(other.minor) != 0) return minor.compareTo(other.minor)
        if (patch.compareTo(other.patch) != 0) return patch.compareTo(other.patch)

        return 0
    }
}
