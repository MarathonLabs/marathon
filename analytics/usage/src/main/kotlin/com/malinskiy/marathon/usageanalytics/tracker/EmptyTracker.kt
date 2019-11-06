package com.malinskiy.marathon.usageanalytics.tracker

internal class EmptyTracker : UsageTracker {
    override fun trackEvent(event: Event) {

    }
}