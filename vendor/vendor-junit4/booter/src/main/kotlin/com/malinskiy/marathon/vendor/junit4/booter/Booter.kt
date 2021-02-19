package com.malinskiy.marathon.vendor.junit4.booter

import com.malinskiy.marathon.vendor.junit4.booter.server.TestExecutorServer

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toInt() ?: 50051
    val server = TestExecutorServer(port)
    server.start()
    server.blockUntilShutdown()
}
