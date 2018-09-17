package com.malinskiy.marathon.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import mu.KLogger
import mu.KotlinLogging

object MarathonLogging {
    var debug = true

    fun logger(func: () -> Unit): KLogger {
        val logger = KotlinLogging.logger(func)
        return changeInternalLogLevel(logger)
    }

    fun logger(name: String): KLogger {
        val logger = KotlinLogging.logger(name)
        return changeInternalLogLevel(logger)
    }

    private fun changeInternalLogLevel(logger: KLogger): KLogger {
        val internalLogger = logger.underlyingLogger as Logger

        if (debug) {
            internalLogger.level = Level.DEBUG
        } else {
            internalLogger.level = Level.ERROR
        }

        return logger
    }
}
