package com.malinskiy.marathon.vendor.junit4.booter

import com.malinskiy.marathon.vendor.junit4.booter.server.BooterServer

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toInt() ?: 50051
    val mode = System.getenv("MODE")?.toString()?.let { Mode.valueOf(it) } ?: Mode.RUNNER

    val server = BooterServer(port, mode)
    server.start()
    server.blockUntilShutdown()
}
