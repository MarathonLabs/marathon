package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.analytics.TrackActionType

data class Event(val action: TrackActionType,
                 val label: String)