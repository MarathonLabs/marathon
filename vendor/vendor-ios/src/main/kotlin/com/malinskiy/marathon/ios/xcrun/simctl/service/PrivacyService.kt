package com.malinskiy.marathon.ios.xcrun.simctl.service

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.xcrun.simctl.SimctlService

class PrivacyService(commandExecutor: CommandExecutor,
                private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {
    /**
     * Grants access to the given service to an application with the given bundle ID
     */
    suspend fun grant(udid: String, service: Permission, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "grant", service.value, bundleId
        )
    }

    /**
     * Revokes access to the given service from an application with the given bundle ID
     */
    suspend fun revoke(udid: String, service: Permission, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "revoke", service.value, bundleId
        )
    }

    /**
     * Resets access to the given service from an application with the given bundle ID
     * This will cause the OS to ask again when this app requests permission to use the given service
     */
    suspend fun reset(udid: String, service: Permission, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "reset", service.value, bundleId
        )
    }

    /**
     * Resets access to the given service from all applications running on the device
     */
    suspend fun resetAll(udid: String, service: Permission): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "reset", service.value
        )
    }
}

/**
 * @property All Apply the action to all services
 * @property Calendar Allow access to calendar
 * @property ContactsLimited Allow access to basic contact info
 * @property Contacts Allow access to full contact details
 * @property Location Allow access to location services when app is in use
 * @property LocationAlways Allow access to location services at all times
 * @property PhotosAdd Allow adding photos to the photo library
 * @property Photos Allow full access to the photo library
 * @property MediaLibrary Allow access to the media library
 * @property Microphone Allow access to audio input
 * @property Motion Allow access to motion and fitness data
 * @property Reminders Allow access to reminders
 * @property Siri Allow use of the app with Siri
 */
enum class Permission(val value: String) {
    All("all"),
    Calendar("calendar"),
    ContactsLimited("contacts-limited"),
    Contacts("contacts"),
    Location("location"),
    LocationAlways("location-always"),
    PhotosAdd("photos-add"),
    Photos("photos"),
    MediaLibrary("media-library"),
    Microphone("microphone"),
    Motion("motion"),
    Reminders("reminders"),
    Siri("siri");
}
