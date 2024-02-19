package com.malinskiy.marathon.apple.logparser.parser

enum class DeviceFailureReason { Unknown, FailedRunner, ConnectionAbort, InvalidSimulatorIdentifier, UnreachableHost, ServicesUnavailable, IncompatibleDevice }

class DeviceFailureException(
    val reason: DeviceFailureReason,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: reason.toString(), cause) {
    constructor(reason: DeviceFailureReason, regex: Regex) : this(reason, regex.pattern)
    constructor(reason: DeviceFailureReason, cause: Throwable) : this(reason, null, cause)
}
