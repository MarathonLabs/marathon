package com.malinskiy.marathon.config.exceptions

class ConfigurationException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
