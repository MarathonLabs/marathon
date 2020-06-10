package com.malinskiy.marathon.android.exception

import com.android.ddmlib.AdbCommandRejectedException

val deviceLostRegex = "device \\'.+\\' not found".toRegex()

fun AdbCommandRejectedException.isDeviceLost(): Boolean {
    if (isDeviceOffline) return true
    return message?.matches(deviceLostRegex) ?: false
}