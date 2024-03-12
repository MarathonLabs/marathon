package com.malinskiy.marathon.device

data class OperatingSystem(val version: String) {
    val major: Int?
        get() = version.split(".").firstOrNull()?.toIntOrNull()
}
