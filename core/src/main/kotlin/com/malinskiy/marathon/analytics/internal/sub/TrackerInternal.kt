package com.malinskiy.marathon.analytics.internal.sub

/**
 * Interface for subscribers of events
 */
interface TrackerInternal {
    fun track(event: Event)
    fun close()
}