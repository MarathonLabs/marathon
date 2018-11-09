package com.malinskiy.marathon.exceptions

class TestBatchExecutionException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}
