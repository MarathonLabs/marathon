package com.malinskiy.marathon.vendor.junit4.booter.server

import com.malinskiy.marathon.vendor.junit4.booter.Mode
import io.grpc.Server
import io.grpc.ServerBuilder

class BooterServer(private val port: Int, private val mode: Mode) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .apply {
            when (mode) {
                Mode.RUNNER -> addService(TestExecutorService())
                Mode.DISCOVER -> addService(TestParserService())
            }
        }
        .maxInboundMessageSize((32 * 1e6).toInt())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** JVM is shutting down: shutting down gRPC server")
                this@BooterServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

}
