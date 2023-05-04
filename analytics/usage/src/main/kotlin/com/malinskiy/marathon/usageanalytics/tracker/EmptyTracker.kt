package com.malinskiy.marathon.usageanalytics.tracker

import com.malinskiy.marathon.usageanalytics.Event

class EmptyTracker : UsageTracker {
    override fun trackEvent(event: Event) = Unit
    override fun meta(version: String, vendor: String, releaseMode: String) = Unit
    override fun close() = Unit
}
