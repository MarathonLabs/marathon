package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.metrics.MetricsProviderProvider
import com.malinskiy.marathon.analytics.tracker.Tracker

class Analytics(tracker: Tracker, val metricsProviderProvider: MetricsProviderProvider) : Tracker by tracker, MetricsProviderProvider by metricsProviderProvider
