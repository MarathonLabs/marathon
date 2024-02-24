package com.malinskiy.marathon.exceptions

/**
 * Indicates that the device cannot be utilized for test run
 */
class IncompatibleDeviceException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
