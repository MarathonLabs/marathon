package com.malinskiy.marathon.usageanalytics.tracker

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.malinskiy.marathon.usageanalytics.Constants
import com.malinskiy.marathon.usageanalytics.Constants.AnalyticsCategory

internal class GoogleAnalyticsTracker() : UsageTracker {

    private val tracker = GoogleAnalytics.builder()
        .withTrackingId(Constants.GoogleAnalyticsId)
        .build()

    override fun trackEvent(event: Event) {
        tracker.event()
            .eventCategory(AnalyticsCategory)
            .eventAction(event.action.name)
            .eventLabel(event.label)
            .sendAsync()
    }
}