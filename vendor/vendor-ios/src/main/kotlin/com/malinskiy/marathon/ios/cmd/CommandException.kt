package com.malinskiy.marathon.ios.cmd

class CommandException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
}
