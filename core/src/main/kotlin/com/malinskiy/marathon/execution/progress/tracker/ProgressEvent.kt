package com.malinskiy.marathon.execution.progress.tracker

sealed class ProgressEvent {
    object Passed : ProgressEvent()
    object Failed : ProgressEvent()
    object Ignored : ProgressEvent()
}