package com.malinskiy.marathon.ios.logparser.parser

enum class DeviceFailureReason { Unknown, FailedRunner, ConnectionAbort, MissingDestination, ServicesUnavailable }

class DeviceFailureException(val reason: DeviceFailureReason,
                             message: String,
                             cause: Throwable? = null): RuntimeException(message, cause) {
    constructor(reason: DeviceFailureReason, regex: Regex): this(reason, regex.pattern)
    constructor(reason: DeviceFailureReason, cause: Throwable): this(reason, "", cause)
}
