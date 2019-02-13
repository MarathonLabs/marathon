package com.malinskiy.marathon.extensions

fun <T> executeGradleCompat(exec: () -> T, fallback: () -> T): T {
    return try {
        exec.invoke()
    } catch (e: NoSuchMethodError) {
        return fallback.invoke()
    }
}