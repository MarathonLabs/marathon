package com.malinskiy.marathon.report

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport

interface Reporter {
    fun generate(executionReport: ExecutionReport)
}