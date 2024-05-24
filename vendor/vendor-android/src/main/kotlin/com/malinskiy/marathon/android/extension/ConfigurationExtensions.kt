package com.malinskiy.marathon.android.extension

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import java.util.concurrent.TimeUnit

fun VideoConfiguration.toScreenRecorderCommand(remoteFilePath: String, device: AndroidDevice? = null): String {
    val sb = StringBuilder()

    sb.append("screenrecord")
    sb.append(' ')

    if (width > 0 && height > 0) {
        sb.append("--size ")
        sb.append(width)
        sb.append('x')
        sb.append(height)
        sb.append(' ')
    }

    if (bitrateMbps > 0) {
        sb.append("--bit-rate ")
        var bitrate = bitrateMbps * 1_000_000
        /**
         * screenrecord supports bitrate up to 200Mbps
         */
        if (bitrate > 200_000_000) {
            bitrate = 200_000_000
        }
        sb.append(bitrate)
        sb.append(' ')
    }

    if (timeLimit > 0) {
        sb.append("--time-limit ")
        var seconds = TimeUnit.SECONDS.convert(timeLimit, timeLimitUnits)
        if (seconds > 180 && ((device?.apiLevel ?: 0) < 34 || !increasedTimeLimitFeatureEnabled)) {
            seconds = 180
        }
        sb.append(seconds)
        sb.append(' ')
    }

    sb.append(remoteFilePath)

    return sb.toString()
}

fun AndroidTestBundleConfiguration.toAndroidTestBundle() = AndroidTestBundle(application, testApplication, extraApplications, splitApks)

fun VendorConfiguration.AndroidConfiguration.testBundlesCompat(): List<AndroidTestBundle> {
    return mutableListOf<AndroidTestBundle>().apply {
        outputs?.let { addAll(it.map { bundleConfiguration -> bundleConfiguration.toAndroidTestBundle() }) }
        testApplicationOutput?.let { it ->
            add(
                AndroidTestBundle(
                    application = applicationOutput,
                    testApplication = it,
                    extraApplications = extraApplicationsOutput,
                    splitApks = splitApks
                )
            )
        }
    }.toList()
}
