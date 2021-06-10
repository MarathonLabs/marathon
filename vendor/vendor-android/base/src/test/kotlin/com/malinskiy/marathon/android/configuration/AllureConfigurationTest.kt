package com.malinskiy.marathon.android.configuration

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class AllureConfigurationTest {
    @Test
    fun testDefault() {
        assertThat(AllureConfiguration().relativeResultsDirectory).isEqualTo("/allure-results")
    }
}
