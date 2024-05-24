package com.malinskiy.marathon.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import mu.KLogger

fun newCoroutineExceptionHandler(logger: KLogger) = CoroutineExceptionHandler { _, exception ->
    when (exception) {
        null -> logger.debug { "CoroutineContext finished" }
        is CancellationException -> logger.debug(exception) { "CoroutineContext cancelled" }
        else -> logger.error(exception) { "CoroutineContext closing due to unrecoverable error" }
    }
}
