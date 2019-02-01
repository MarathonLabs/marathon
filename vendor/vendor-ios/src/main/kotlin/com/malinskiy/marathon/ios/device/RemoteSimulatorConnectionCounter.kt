package com.malinskiy.marathon.ios.device

import java.util.Collections

object RemoteSimulatorConnectionCounter {
    private val udids = Collections.synchronizedList(mutableListOf<String>())

    fun get(udid: String): Int = udids.count { it == udid }

    fun putAndGet(udid: String): Int {
        udids.add(udid)
        return get(udid)
    }
}
