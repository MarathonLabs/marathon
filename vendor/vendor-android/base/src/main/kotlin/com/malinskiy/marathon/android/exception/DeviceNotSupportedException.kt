package com.malinskiy.marathon.android.exception

class DeviceNotSupportedException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
