package com.malinskiy.marathon.lite.configuration

enum class ScreenRecordingPolicy {
    ON_FAILURE,
    ON_ANY;
}

fun ScreenRecordingPolicy.toProto(): com.malinskiy.marathon.cliconfig.proto.ScreenRecordingPolicy {
    return when (this) {
        ScreenRecordingPolicy.ON_FAILURE -> com.malinskiy.marathon.cliconfig.proto.ScreenRecordingPolicy.ON_FAILURE
        ScreenRecordingPolicy.ON_ANY -> com.malinskiy.marathon.cliconfig.proto.ScreenRecordingPolicy.ON_ANY
    }
}
