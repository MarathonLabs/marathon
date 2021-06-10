package com.malinskiy.marathon.android

import com.malinskiy.marathon.device.DeviceFeature

object RecorderTypeSelector {
    fun selectRecorderType(supportedFeatures: Collection<DeviceFeature>, configuration: ScreenRecordConfiguration): DeviceFeature? {
        val preferred = configuration.preferableRecorderType
        val screenshotEnabled = recorderEnabled(DeviceFeature.SCREENSHOT, configuration)
        val videoEnabled = recorderEnabled(DeviceFeature.VIDEO, configuration)

        if (preferred != null && supportedFeatures.contains(preferred) && recorderEnabled(preferred, configuration)) {
            return preferred
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
