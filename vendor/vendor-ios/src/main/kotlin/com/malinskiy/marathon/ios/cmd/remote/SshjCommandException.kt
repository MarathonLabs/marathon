package com.malinskiy.marathon.ios.cmd.remote

class SshjCommandException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
}
