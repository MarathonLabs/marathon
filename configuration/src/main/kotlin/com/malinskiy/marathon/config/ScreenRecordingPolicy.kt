package com.malinskiy.marathon.config

/**
 * Defines when screen recordings should be kept.
 */
enum class ScreenRecordingPolicy {
    /**
     * Keep screen recording only if the test failed. (Default)
     */
    ON_FAILURE,
    /**
     * Keep screen recording in any case.
     */
    ON_ANY;
}
