package com.malinskiy.marathon.usageanalytics

import com.malinskiy.marathon.usageanalytics.tracker.EmptyTracker
import com.malinskiy.marathon.usageanalytics.tracker.GoogleAnalyticsTracker
import com.malinskiy.marathon.usageanalytics.tracker.Tracker

object UsageAnalytics {
    var enable = false

    val tracker: Tracker by lazy {
        if (enable) GoogleAnalyticsTracker() else EmptyTracker()
    }
}
