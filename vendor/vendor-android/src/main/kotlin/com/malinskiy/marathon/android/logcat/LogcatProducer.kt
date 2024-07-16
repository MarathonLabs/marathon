package com.malinskiy.marathon.android.logcat

interface LogcatProducer {
    fun addLogcatListener(listener: LogcatListener)
    fun removeLogcatListener(listener: LogcatListener)
}
