package com.malinskiy.marathon.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import mu.KLogger
import mu.KotlinLogging

object MarathonLogging {
    var debug = true

    fun logger(func: () -> Unit): KLogger {
        return logger(level = null, func = func)
    }

    fun logger(name: String) :KLogger {
        return logger(level = null, name = name)
    }

    fun logger(level: Level?, func: () -> Unit): KLogger {
        val logger = KotlinLogging.logger(func)
        return changeInternalLogLevel(logger, level = level)
    }

    fun logger(level: Level?, name: String): KLogger {
        val logger = KotlinLogging.logger(name)
        return changeInternalLogLevel(logger, level = level)
    }

    private fun changeInternalLogLevel(logger: KLogger, level: Level?): KLogger {
        val internalLogger = logger.underlyingLogger as Logger
        internalLogger.level = level
        ?: when {
            debug -> Level.DEBUG
            else -> Level.ERROR
        }

        return logger
    }
}
