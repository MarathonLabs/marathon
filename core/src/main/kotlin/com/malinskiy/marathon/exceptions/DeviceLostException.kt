package com.malinskiy.marathon.exceptions

/**
 * Indicates that the execution device is no longer available
 */
class DeviceLostException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}