package com.malinskiy.marathon.config.vendor.ios

import com.malinskiy.marathon.config.vendor.apple.ios.ApplicationPermissionGrant
import com.malinskiy.marathon.config.vendor.apple.ios.ApplicationPermissionsConfiguration
import com.malinskiy.marathon.config.vendor.apple.ios.GrantLifecycle
import com.malinskiy.marathon.config.vendor.apple.ios.Permission
import com.malinskiy.marathon.config.vendor.apple.ios.PermissionsConfiguration
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class PermissionsConfigurationTest {
    @Test
    fun `legacy grant targets application under test`() {
        val configuration = PermissionsConfiguration(
            grant = setOf(Permission.Contacts),
            lifecycle = GrantLifecycle.BEFORE_EACH_BATCH,
        )

        configuration.resolve("com.example.app") shouldBeEqualTo listOf(
            ApplicationPermissionGrant(
                bundleId = "com.example.app",
                permissions = setOf(Permission.Contacts),
            )
        )
    }

    @Test
    fun `application grants support multiple bundle identifiers with shared lifecycle`() {
        val configuration = PermissionsConfiguration(
            lifecycle = GrantLifecycle.BEFORE_TEST_RUN,
            applications = mapOf(
                "com.apple.Maps" to ApplicationPermissionsConfiguration(
                    grant = setOf(Permission.Location),
                ),
                "com.example.companion" to ApplicationPermissionsConfiguration(
                    grant = setOf(Permission.Microphone),
                ),
            ),
        )

        configuration.resolve("com.example.app").toSet() shouldBeEqualTo setOf(
            ApplicationPermissionGrant(
                bundleId = "com.apple.Maps",
                permissions = setOf(Permission.Location),
            ),
            ApplicationPermissionGrant(
                bundleId = "com.example.companion",
                permissions = setOf(Permission.Microphone),
            ),
        )
    }

    @Test
    fun `empty grants are ignored`() {
        val configuration = PermissionsConfiguration(
            applications = mapOf(
                "com.apple.Maps" to ApplicationPermissionsConfiguration(),
            ),
        )

        configuration.resolve("com.example.app") shouldBeEqualTo emptyList()
    }
}
