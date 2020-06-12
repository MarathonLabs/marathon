package com.malinskiy.marathon.exceptions

/**
 * Indicates that the batch execution timed out
 */
class TestBatchTimeoutException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(message: String): super(message)
}
