package com.malinskiy.marathon.usageanalytics.tracker

interface UsageTracker{
    fun trackEvent(event: Event)
}