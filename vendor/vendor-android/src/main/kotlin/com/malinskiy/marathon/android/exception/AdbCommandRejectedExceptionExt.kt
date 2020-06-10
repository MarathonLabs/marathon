package com.malinskiy.marathon.android.exception

import com.android.ddmlib.AdbCommandRejectedException

val deviceLostRegex = Regex("device \\'.+\\' not found")

fun AdbCommandRejectedException.isDeviceLost(): Boolean {
    if (isDeviceOffline) return true
    return message?.matches(deviceLostRegex) ?: false
}