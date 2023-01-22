package com.malinskiy.marathon.exceptions

class TransferException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, reason: Throwable) : super(message, reason)
    constructor(t: Throwable) : super(t)
}
