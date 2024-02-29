package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class PermissionsConfiguration(
    @JsonProperty("grant") val grant: Set<Permission> = emptySet(),
    @JsonProperty("lifecycle") val lifecycle: GrantLifecycle = GrantLifecycle.BEFORE_TEST_RUN,
)

enum class GrantLifecycle {
    @JsonProperty("BEFORE_TEST_RUN") BEFORE_TEST_RUN,
    @JsonProperty("BEFORE_EACH_BATCH") BEFORE_EACH_BATCH,
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
    @JsonProperty("all") All("all"),
    @JsonProperty("calendar") Calendar("calendar"),
    @JsonProperty("contacts-limited") ContactsLimited("contacts-limited"),
    @JsonProperty("contacts") Contacts("contacts"),
    @JsonProperty("location") Location("location"),
    @JsonProperty("location-always") LocationAlways("location-always"),
    @JsonProperty("photos-add") PhotosAdd("photos-add"),
    @JsonProperty("photos") Photos("photos"),
    @JsonProperty("media-library") MediaLibrary("media-library"),
    @JsonProperty("microphone") Microphone("microphone"),
    @JsonProperty("motion") Motion("motion"),
    @JsonProperty("reminders") Reminders("reminders"),
    @JsonProperty("siri") Siri("siri"),

    //Non-simctl grant permissions
    @JsonProperty("user-tracking") UserTracking("kTCCServiceUserTracking"),
}
