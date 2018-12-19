package com.malinskiy.marathon.usageanalytics.tracker

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.malinskiy.marathon.usageanalytics.Constants

internal class GoogleAnalyticsTracker() : Tracker {

    private val tracker = GoogleAnalytics.builder()
            .withTrackingId(Constants.GoogleAnalyticsId)
            .build()

    override fun trackEvent(event: Event) {
        tracker.event().eventAction(event.action.name).eventLabel(event.label).sendAsync()
    }
}