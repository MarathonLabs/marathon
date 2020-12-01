package com.malinskiy.marathon.report.junit.model

data class StackTraceElement(
    val isError: Boolean,
    val stackTrace: String?
)
