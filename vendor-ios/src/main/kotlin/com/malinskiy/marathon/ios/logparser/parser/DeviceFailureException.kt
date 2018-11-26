package com.malinskiy.marathon.ios.logparser.parser

class DeviceFailureException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}
