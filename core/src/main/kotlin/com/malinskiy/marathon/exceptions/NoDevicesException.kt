package com.malinskiy.marathon.exceptions

class NoDevicesException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
