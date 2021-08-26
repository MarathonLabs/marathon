package com.malinskiy.marathon.vendor.junit4.booter

import com.malinskiy.marathon.vendor.junit4.booter.exec.ExecutionMode
import com.malinskiy.marathon.vendor.junit4.booter.server.BooterServer

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toInt() ?: 50051
    val booterMode = System.getenv("MODE")?.toString()?.let { Mode.valueOf(it) } ?: Mode.RUNNER
    val executionMode = System.getenv("EXEC_MODE")?.toString()?.let { ExecutionMode.valueOf(it) } ?: ExecutionMode.ISOLATED

    val server = BooterServer(port, booterMode, executionMode)
    server.start()
    server.blockUntilShutdown()
}
