package com.malinskiy.marathon.android.exception

class TransferException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
}
