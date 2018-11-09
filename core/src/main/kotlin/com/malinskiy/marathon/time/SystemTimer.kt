package com.malinskiy.marathon.time

import kotlin.system.measureTimeMillis

class SystemTimer : Timer {
    override fun currentTimeMillis() = System.currentTimeMillis()
    override fun measure(block: () -> Unit) = measureTimeMillis(block)
}