package com.malinskiy.marathon

import com.malinskiy.marathon.execution.Configuration

class ShutdownHook(
    configuration: Configuration,
    private val runtime: Runtime = Runtime.getRuntime(),
    val block: () -> Unit
) : Thread() {
    private val debug = configuration.debug

    override fun run() {
        block()
    }

    fun install() {
        return when (debug) {
            true -> try {
                runtime.addShutdownHook(this)
            } catch (e: IllegalStateException) {
            } catch (e: SecurityException) {
            }
            else -> Unit
        }
    }

    fun uninstall(): Boolean {
        return when (debug) {
            true -> try {
                runtime.removeShutdownHook(this)
                true
            } catch (e: IllegalStateException) {
                false
            } catch (e: SecurityException) {
                false
            }
            else -> true
        }
    }
}
