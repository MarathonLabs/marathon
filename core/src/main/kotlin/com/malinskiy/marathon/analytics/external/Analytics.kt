package com.malinskiy.marathon.analytics.external

class Analytics(val metricsProvider: MetricsProvider) : MetricsProvider by metricsProvider
