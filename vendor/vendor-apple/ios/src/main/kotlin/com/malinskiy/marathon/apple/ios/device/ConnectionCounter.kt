package com.malinskiy.marathon.apple.ios.device

import com.malinskiy.marathon.apple.ios.configuration.Transport

class ConnectionCounter {
    private val transports = HashMap<Transport, Int>().withDefault { 0 }
    private val lock = Object()

    fun get(transport: Transport): Int {
        synchronized(lock) {
            return transports[transport] ?: 0
        }
    }
    
    fun increment(transport: Transport): Int {
        synchronized(lock) {
            val newValue = (transports[transport] ?: 0) + 1
            transports[transport] = newValue
            return newValue
        }
    }
    
    fun decrementAndGet(transport: Transport): Int {
        synchronized(lock) {
            val newValue = (transports[transport] ?: 0) -1
            transports[transport] = newValue
            return newValue
        }
    }
}
