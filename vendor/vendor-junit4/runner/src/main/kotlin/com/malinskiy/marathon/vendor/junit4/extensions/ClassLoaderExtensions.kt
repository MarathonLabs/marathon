package com.malinskiy.marathon.vendor.junit4.extensions

inline fun <T> ClassLoader.switch(block: () -> T): T {
    val originalClassLoader = Thread.currentThread().contextClassLoader
    return try {
        Thread.currentThread().contextClassLoader = this
        block.invoke()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    } finally {
        Thread.currentThread().contextClassLoader = originalClassLoader
    }
}
