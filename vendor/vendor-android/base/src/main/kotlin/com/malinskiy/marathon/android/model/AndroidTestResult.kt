package com.malinskiy.marathon.android.model

data class AndroidTestResult(
    var status: AndroidTestStatus = AndroidTestStatus.INCOMPLETE,
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long = 0,
    var stackTrace: String? = null,
    var metrics: Map<String, String>? = null
)