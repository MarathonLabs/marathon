package com.malinskiy.marathon.usageanalytics.tracker

import com.malinskiy.marathon.usageanalytics.TrackActionType

data class Event(
    val action: TrackActionType,
    val label: String
)