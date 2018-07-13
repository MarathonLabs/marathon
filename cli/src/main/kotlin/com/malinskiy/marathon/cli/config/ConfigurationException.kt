package com.malinskiy.marathon.cli.config

class ConfigurationException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}
