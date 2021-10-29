package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.analytics.external.MetricsProviderFactory
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.io.File

class MetricsProviderFactoryTest {

    @Test
    fun shouldReturnNoopProviderWhenDisabled() {
        val configuration = Configuration.Builder(
            name = "",
            outputDir = File(""),
            vendorConfiguration = VendorConfiguration.StubVendorConfiguration,
        ).apply {
            analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
            analyticsTracking = false
        }.build()
        val factory = MetricsProviderFactory(configuration)
        val metricsProvider = factory.create()
        metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
    }

    @Test
    fun shouldReturnNoopProviderWhenConfigurationIsInvalid() {
        val analyticsConfiguration = AnalyticsConfiguration.InfluxDbConfiguration(
            "host",
            "user",
            "password",
            "db",
            AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
        )
        val configuration = Configuration.Builder(
            name = "",
            outputDir = File(""),
            vendorConfiguration = VendorConfiguration.StubVendorConfiguration,
        ).apply {
            this.analyticsConfiguration = analyticsConfiguration
            analyticsTracking = false
        }.build()
        val factory = MetricsProviderFactory(configuration)
        val metricsProvider = factory.create()
        metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
    }
}
