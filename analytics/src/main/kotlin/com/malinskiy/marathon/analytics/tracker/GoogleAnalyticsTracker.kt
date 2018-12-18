package com.malinskiy.marathon.analytics.tracker

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.malinskiy.marathon.analytics.Constants

internal class GoogleAnalyticsTracker : Tracker {

    private val tracker = GoogleAnalytics.builder()
            .withTrackingId(Constants.GoogleAnalyticsId)
            .build()

    override fun trackEvent(event: Event) {
        tracker.event().eventAction(event.action).eventLabel(event.label).sendAsync()
    }
}