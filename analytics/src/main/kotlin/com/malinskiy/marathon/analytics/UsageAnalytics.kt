package com.malinskiy.marathon.analytics

interface Analytics{
    fun trackEvent(event: Event)
}