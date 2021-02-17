package com.malinskiy.marathon.exceptions

class DeviceSetupException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
