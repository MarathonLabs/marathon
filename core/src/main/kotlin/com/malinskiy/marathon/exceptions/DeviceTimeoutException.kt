package com.malinskiy.marathon.exceptions

/**
 * Indicates that device get stuck: timeout exceeded but ddmlib didn't stop run
 */
class DeviceTimeoutException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}