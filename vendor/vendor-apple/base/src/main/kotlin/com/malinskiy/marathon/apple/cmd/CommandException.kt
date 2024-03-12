package com.malinskiy.marathon.apple.cmd

class CommandException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
}
