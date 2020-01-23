package com.malinskiy.marathon.exceptions

import com.malinskiy.marathon.config.AppType

interface ExceptionsReporter {
    fun start(appType: AppType)
    fun end()
}
