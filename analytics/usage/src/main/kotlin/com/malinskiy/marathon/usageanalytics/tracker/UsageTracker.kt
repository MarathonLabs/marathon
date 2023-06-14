package com.malinskiy.marathon.usageanalytics.tracker

import com.malinskiy.marathon.usageanalytics.Event

interface UsageTracker {
    fun trackEvent(event: Event)
    fun meta(version: String, vendor: String, releaseMode: String)
    fun close()
}
