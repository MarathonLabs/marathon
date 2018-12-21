package com.malinskiy.marathon.usageanalytics.tracker

interface Tracker{
    fun trackEvent(event: Event)
}