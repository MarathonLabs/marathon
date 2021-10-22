package com.malinskiy.marathon.android

import com.malinskiy.marathon.config.vendor.android.RecorderType
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.device.DeviceFeature

object RecorderTypeSelector {
    fun selectRecorderType(supportedFeatures: Collection<DeviceFeature>, configuration: ScreenRecordConfiguration): DeviceFeature? {
        val preferredFeature = when(configuration.preferableRecorderType) {
            RecorderType.VIDEO -> DeviceFeature.VIDEO
            RecorderType.SCREENSHOT -> DeviceFeature.SCREENSHOT
            null -> null
        }
        
        val screenshotEnabled = recorderEnabled(DeviceFeature.SCREENSHOT, configuration)
        val videoEnabled = recorderEnabled(DeviceFeature.VIDEO, configuration)

        if (preferredFeature != null && supportedFeatures.contains(preferredFeature) && recorderEnabled(preferredFeature, configuration)) {
            return preferredFeature
        }

        return when {
            supportedFeatures.contains(DeviceFeature.VIDEO) && videoEnabled -> DeviceFeature.VIDEO
            supportedFeatures.contains(DeviceFeature.SCREENSHOT) && screenshotEnabled -> DeviceFeature.SCREENSHOT
            else -> null
        }
    }

    private fun recorderEnabled(
        type: DeviceFeature,
        configuration: ScreenRecordConfiguration
    ) = when (type) {
        DeviceFeature.VIDEO -> configuration.videoConfiguration.enabled
        DeviceFeature.SCREENSHOT -> configuration.screenshotConfiguration.enabled
    }

}
