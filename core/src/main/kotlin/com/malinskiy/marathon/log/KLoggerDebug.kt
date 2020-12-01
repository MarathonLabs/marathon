package com.malinskiy.marathon.log

import mu.KLogger
import org.slf4j.Marker

class KLoggerDebug(override val underlyingLogger: KLogger) : KLogger by underlyingLogger {
    override fun debug(msg: () -> Any?) {
        warn(msg)
    }

    override fun info(msg: () -> Any?) {
        warn(msg)
    }

    override fun trace(msg: () -> Any?) {
        warn(msg)
    }

    override fun debug(format: String, vararg arguments: Any) {
        warn(format, arguments)
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        warn(marker, format, arguments)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        warn(marker, format, arg)
    }

    override fun debug(format: String?, arg: Any?) {
        warn(format, arg)
    }

    override fun debug(marker: Marker?, msg: String?) {
        warn(marker, msg)
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        warn(format, arg1, arg2)
    }

    override fun debug(msg: String?, t: Throwable?) {
        warn(msg, t)
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        warn(marker, msg, t)
    }

    override fun debug(msg: String?) {
        warn(msg)
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        warn(marker, format, arg1, arg2)
    }

    override fun info(marker: Marker?, msg: String?) {
        warn(marker, msg)
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        warn(format, arg1, arg2)
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        warn(marker, format, arguments)
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        warn(marker, msg, t)
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        warn(marker, format, arg1, arg2)
    }

    override fun info(msg: String?) {
        warn(msg)
    }

    override fun info(format: String?, vararg arguments: Any?) {
        warn(format, arguments)
    }

    override fun info(msg: String?, t: Throwable?) {
        warn(msg, t)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        warn(marker, format, arg)
    }

    override fun info(format: String?, arg: Any?) {
        warn(format, arg)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        warn(format, arg1, arg2)
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        warn(marker, msg, t)
    }

    override fun trace(msg: String?) {
        warn(msg)
    }

    override fun trace(format: String?, arg: Any?) {
        warn(format, arg)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        warn(marker, format, arg)
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        warn(marker, format, arg1, arg2)
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        warn(marker, format, argArray)
    }

    override fun trace(msg: String?, t: Throwable?) {
        warn(msg, t)
    }

    override fun trace(marker: Marker?, msg: String?) {
        warn(marker, msg)
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        warn(format, arguments)
    }
}
