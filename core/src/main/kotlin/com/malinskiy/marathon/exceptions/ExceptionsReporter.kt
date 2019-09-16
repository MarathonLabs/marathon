package com.malinskiy.marathon.exceptions

interface ExceptionsReporter {
    fun start(appType: AppType)
}