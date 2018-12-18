package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.tracker.GoogleAnalyticsTracker
import com.malinskiy.marathon.analytics.tracker.Tracker

object UsageAnalytics{
    val tracker: Tracker by lazy {
        GoogleAnalyticsTracker()
    }
}
