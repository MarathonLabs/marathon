package com.malinskiy.marathon.exceptions

class ConfigurationException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}
