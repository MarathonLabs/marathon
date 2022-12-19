package com.malinskiy.marathon.ios.device

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

object RemoteSimulatorConnectionCounter {
    private val udids = HashMap<String, Int>()
    private val lock = Object()

    fun get(udid: String): Int {
        synchronized(lock) {
            return udids[udid] ?: 0
        }
    }

    fun putAndGet(udid: String): Int {
        synchronized(lock) {
            val newValue = (udids[udid] ?: 0) + 1
            udids[udid] = newValue
            return newValue
        }
    }
}
