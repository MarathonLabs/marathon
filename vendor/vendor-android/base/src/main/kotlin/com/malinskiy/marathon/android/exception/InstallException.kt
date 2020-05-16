package com.malinskiy.marathon.android.exception

class InstallException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
