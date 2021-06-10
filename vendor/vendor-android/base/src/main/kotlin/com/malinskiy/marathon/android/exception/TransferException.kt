package com.malinskiy.marathon.android.exception

class TransferException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, reason: Throwable) : super(message, reason)
    constructor(t: Throwable) : super(t)
}
