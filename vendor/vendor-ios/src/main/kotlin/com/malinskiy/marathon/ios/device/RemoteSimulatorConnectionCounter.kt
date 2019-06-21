package com.malinskiy.marathon.ios.device

import java.util.*

object RemoteSimulatorConnectionCounter {
    private val udids = Collections.synchronizedList(mutableListOf<String>())

    fun get(udid: String): Int = synchronized(this) { udids.count { it == udid } }

    fun putAndGet(udid: String): Int = synchronized(this) {
        udids.add(udid)
        return get(udid)
    }
}
