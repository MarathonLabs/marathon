package com.malinskiy.marathon.apple.bin.xcrun.simctl.model

import com.google.gson.annotations.SerializedName

/**
 * Example json:
 *       "productFamily": "Apple Watch",
 *       "bundlePath": "/Applications/Xcode.app/Contents/Developer/Platforms/WatchOS.platform/Library/Developer/CoreSimulator/Profiles/DeviceTypes/Apple Watch Series 8 (41mm).simdevicetype",
 *       "maxRuntimeVersion": 4294967295,
 *       "maxRuntimeVersionString": "65535.255.255",
 *       "identifier": "com.apple.CoreSimulator.SimDeviceType.Apple-Watch-Series-8-41mm",
 *       "modelIdentifier": "Watch6,14",
 *       "minRuntimeVersionString": "9.0.0",
 *       "minRuntimeVersion": 589824,
 *       "name": "Apple Watch Series 8 (41mm)"
 */
data class SimctlDeviceType(
    @SerializedName("name") val name: String,
    @SerializedName("identifier") val identifier: String
)
