package com.malinskiy.marathon.android.configuration

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import org.junit.jupiter.api.Test

class AllureConfigurationTest {
    @Test
    fun testDefault() {
        assertThat(AllureConfiguration().relativeResultsDirectory).isEqualTo("/files/allure-results")
    }
}
