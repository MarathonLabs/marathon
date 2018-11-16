package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.analytics.tracker.Tracker

class Analytics(tracker: Tracker, val metricsProvider: MetricsProvider) : Tracker by tracker, MetricsProvider by metricsProvider
