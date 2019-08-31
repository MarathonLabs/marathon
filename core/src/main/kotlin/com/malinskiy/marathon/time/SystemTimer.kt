package com.malinskiy.marathon.time

import kotlin.system.measureTimeMillis

class SystemTimer : Timer {
    override val startTimeMillis = System.currentTimeMillis()
    override val elapsedTimeMillis: Long
        get() = System.currentTimeMillis() - startTimeMillis

    override fun currentTimeMillis() = System.currentTimeMillis()
    override fun measure(block: () -> Unit) = measureTimeMillis(block)
}