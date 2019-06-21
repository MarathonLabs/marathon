package com.malinskiy.marathon.analytics.metrics

interface MetricsProviderProvider {
    fun create(): MetricsProvider
}