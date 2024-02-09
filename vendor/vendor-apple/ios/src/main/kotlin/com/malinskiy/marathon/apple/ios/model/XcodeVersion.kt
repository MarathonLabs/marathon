package com.malinskiy.marathon.apple.ios.model

import com.malinskiy.marathon.exceptions.DeviceSetupException

data class XcodeVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<XcodeVersion> {

    companion object {
        fun from(value: String): XcodeVersion {
            val components = value.trim().split('.').map { it.trim().toInt() }
            return when (components.size) {
                1 -> XcodeVersion(components[0], 0, 0)
                2 -> XcodeVersion(components[0], components[1], 0)
                3 -> XcodeVersion(components[0], components[1], components[2])
                else -> throw DeviceSetupException("Invalid xcode version: $value. Expecting semi-symver format")
            }
        }
    }

    override fun compareTo(other: XcodeVersion): Int {
        val majorDiff = major - other.major
        when {
            majorDiff > 0 -> return majorDiff
            majorDiff < 0 -> return majorDiff
            else -> Unit
        }

        val minorDiff = minor - other.minor
        when {
            minorDiff > 0 -> return minorDiff
            minorDiff < 0 -> return minorDiff
            else -> Unit
        }
        
        val patchDiff = patch - other.patch
        return when {
            patchDiff > 0 -> patchDiff
            patchDiff < 0 -> patchDiff
            else -> 0
        }
    }
}
