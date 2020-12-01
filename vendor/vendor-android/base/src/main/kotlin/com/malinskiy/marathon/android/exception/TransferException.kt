package com.malinskiy.marathon.android.exception

class TransferException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(t: Throwable) : super(t)
}
