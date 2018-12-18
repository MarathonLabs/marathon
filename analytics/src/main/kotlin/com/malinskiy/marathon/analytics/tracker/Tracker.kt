package com.malinskiy.marathon.analytics.tracker

interface Tracker{
    fun trackEvent(event: Event)
}