package com.malinskiy.marathon.usageanalytics

import com.malinskiy.marathon.usageanalytics.tracker.EmptyTracker
import com.malinskiy.marathon.usageanalytics.tracker.GoogleAnalyticsTracker
import com.malinskiy.marathon.usageanalytics.tracker.UsageTracker

object UsageAnalytics {
    var enable = false

    val USAGE_TRACKER: UsageTracker by lazy {
        if (enable) GoogleAnalyticsTracker() else EmptyTracker()
    }
}
