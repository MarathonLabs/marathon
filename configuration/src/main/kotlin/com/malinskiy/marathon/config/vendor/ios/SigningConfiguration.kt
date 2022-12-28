package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @param The path of the provisioning profile of the generated test runner app.
 *      If this field is not set, will use app under test's provisioning profile
 *      for the generated test runner app.
 */
data class SigningConfiguration(
    @JsonProperty("testRunnerProvisioningProfile") val testRunnerProvisioningProfile: String? = null
)
