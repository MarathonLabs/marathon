package com.malinskiy.marathon.report.logs

import com.malinskiy.marathon.execution.listener.LineListener

interface LogProducer {
    fun addLineListener(listener: LineListener)
    fun removeLineListener(listener: LineListener)
}
