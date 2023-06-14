package com.malinskiy.marathon.device.screenshot

import java.awt.image.BufferedImage
import java.time.Duration

interface Screenshottable {
    val orientation: Rotation

    /**
     * @return screenshot or null if there was a failure
     */
    suspend fun getScreenshot(timeout: Duration): BufferedImage?
}
