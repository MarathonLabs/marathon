package com.malinskiy.marathon.log

import com.github.ajalt.mordant.rendering.TextColors
import com.malinskiy.marathon.log.TerminalPrettyOutput.terminal
import mu.KLogger
import mu.Marker

class MordantLogger(override val underlyingLogger: KLogger) : KLogger by underlyingLogger {

    override fun trace(msg: () -> Any?) {
//        terminal.println(TextColors.blue(msg.toStringSafe()))
    }
    override fun trace(t: Throwable?, msg: () -> Any?) {}
    override fun trace(marker: mu.Marker?, msg: () -> Any?) {}
    override fun trace(marker: mu.Marker?, t: Throwable?, msg: () -> Any?) {}
    override fun trace(p0: String?) {}
    override fun trace(p0: String?, p1: Any?) {}
    override fun trace(p0: String?, p1: Any?, p2: Any?) {}
    override fun trace(p0: String?, vararg p1: Any?) {}
    override fun trace(p0: String?, p1: Throwable?) {}
    override fun trace(p0: Marker?, p1: String?) {}
    override fun trace(p0: Marker?, p1: String?, p2: Any?) {}
    override fun trace(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {}
    override fun trace(p0: Marker?, p1: String?, vararg p2: Any?) {}
    override fun trace(p0: Marker?, p1: String?, p2: Throwable?) {}

    override fun debug(msg: () -> Any?) {
//        terminal.println(TextColors.white(msg.toStringSafe()))
    }
    override fun debug(t: Throwable?, msg: () -> Any?) {}
    override fun debug(marker: mu.Marker?, msg: () -> Any?) {}
    override fun debug(marker: mu.Marker?, t: Throwable?, msg: () -> Any?) {}
    override fun debug(format: String, vararg arguments: Any) {}
    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun debug(marker: Marker?, format: String?, arg: Any?) {}
    override fun debug(format: String?, arg: Any?) {}
    override fun debug(marker: Marker?, msg: String?) {}
    override fun debug(format: String?, arg1: Any?, arg2: Any?) {}
    override fun debug(msg: String?, t: Throwable?) {}
    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {}
    override fun entry(vararg argArray: Any?) {}
    override fun debug(msg: String?) {}
    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}

    override fun info(msg: () -> Any?) {
//        terminal.println(TextColors.green(msg.toStringSafe()))
    }
    override fun info(t: Throwable?, msg: () -> Any?) {}
    override fun info(marker: mu.Marker?, msg: () -> Any?) {}
    override fun info(marker: mu.Marker?, t: Throwable?, msg: () -> Any?) {}
    override fun info(p0: String?) {}
    override fun info(p0: String?, p1: Any?) {}
    override fun info(p0: String?, p1: Any?, p2: Any?) {}
    override fun info(p0: String?, vararg p1: Any?) {}
    override fun info(p0: String?, p1: Throwable?) {}
    override fun info(p0: Marker?, p1: String?) {}
    override fun info(p0: Marker?, p1: String?, p2: Any?) {}
    override fun info(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {}
    override fun info(p0: Marker?, p1: String?, vararg p2: Any?) {}
    override fun info(p0: Marker?, p1: String?, p2: Throwable?) {}

    override fun warn(msg: () -> Any?) {
        terminal.println(TextColors.yellow(msg.toStringSafe()))
    }
    override fun warn(t: Throwable?, msg: () -> Any?) {}
    override fun warn(marker: mu.Marker?, msg: () -> Any?) {}
    override fun warn(marker: mu.Marker?, t: Throwable?, msg: () -> Any?) {}
    override fun warn(p0: String?) {}
    override fun warn(p0: String?, p1: Any?) {}
    override fun warn(p0: String?, vararg p1: Any?) {}
    override fun warn(p0: String?, p1: Any?, p2: Any?) {}
    override fun warn(p0: String?, p1: Throwable?) {}
    override fun warn(p0: Marker?, p1: String?) {}
    override fun warn(p0: Marker?, p1: String?, p2: Any?) {}
    override fun warn(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {}
    override fun warn(p0: Marker?, p1: String?, vararg p2: Any?) {}
    override fun warn(p0: Marker?, p1: String?, p2: Throwable?) {}

    override fun error(msg: () -> Any?) {
        terminal.println(TextColors.brightRed(msg.toStringSafe()))
    }
    override fun error(t: Throwable?, msg: () -> Any?) {}
    override fun error(marker: mu.Marker?, msg: () -> Any?) {}
    override fun error(marker: mu.Marker?, t: Throwable?, msg: () -> Any?) {}
    override fun error(p0: String?) {}
    override fun error(p0: String?, p1: Any?) {}
    override fun error(p0: String?, p1: Any?, p2: Any?) {}
    override fun error(p0: String?, vararg p1: Any?) {}
    override fun error(p0: String?, p1: Throwable?) {}
    override fun error(p0: Marker?, p1: String?) {}
    override fun error(p0: Marker?, p1: String?, p2: Any?) {}
    override fun error(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {}
    override fun error(p0: Marker?, p1: String?, vararg p2: Any?) {}
    override fun error(p0: Marker?, p1: String?, p2: Throwable?) {}

    @Suppress("NOTHING_TO_INLINE")
    private inline fun (() -> Any?).toStringSafe(): String {
        return try {
            invoke().toString()
        } catch (e: Exception) {
            return "Log message invocation failed: $e"
        }
    }
}
