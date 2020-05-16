package com.malinskiy.marathon.extensions

fun <T> executeGradleCompat(exec: () -> T, fallbacks: List<() -> T>): T {
    return try {
        exec.invoke()
    } catch (e: NoSuchMethodError) {
        for (fallback in fallbacks) {
            try {
                return fallback.invoke()
            } catch (e: Exception) {
                //Try next fallback
            }
        }

        throw RuntimeException("Unable to execute gradle compat: all fallbacks failed")
    }
}
