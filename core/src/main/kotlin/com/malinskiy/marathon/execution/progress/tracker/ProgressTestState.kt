package com.malinskiy.marathon.execution.progress.tracker

sealed class ProgressTestState {
    object Started : ProgressTestState()
    object Passed : ProgressTestState()
    object Failed : ProgressTestState()
    object Ignored : ProgressTestState()
}