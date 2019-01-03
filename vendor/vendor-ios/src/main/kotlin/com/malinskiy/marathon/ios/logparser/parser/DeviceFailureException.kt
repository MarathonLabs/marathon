package com.malinskiy.marathon.ios.logparser.parser

enum class DeviceFailureReason { Unknown, FailedRunner, ConnectionAbort, MissingDestination, ServicesUnavailable }

class DeviceFailureException(val reason: DeviceFailureReason, message: String): RuntimeException(message) {
    constructor(reason: DeviceFailureReason, regex: Regex): this(reason, regex.pattern)
}
