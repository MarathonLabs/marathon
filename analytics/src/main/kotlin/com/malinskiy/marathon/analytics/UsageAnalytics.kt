package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.tracker.EmptyTracker
import com.malinskiy.marathon.analytics.tracker.GoogleAnalyticsTracker
import com.malinskiy.marathon.analytics.tracker.Tracker

object UsageAnalytics {
    var enable = false

    val tracker: Tracker by lazy {
        if (enable) GoogleAnalyticsTracker() else EmptyTracker()
    }
}
