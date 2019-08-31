package com.malinskiy.marathon.analytics.internal.sub

class DelegatingTrackerInternal(private val trackers: List<TrackerInternal>): TrackerInternal {
    override fun track(event: Event) = trackers.forEach { it.track(event) }

    override fun close() = trackers.forEach { it.close() }
}